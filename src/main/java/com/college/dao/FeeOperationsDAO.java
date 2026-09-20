package com.college.dao;

import com.college.models.FeePayment;
import com.college.utils.DatabaseConnection;
import com.college.utils.Logger;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

/** Advanced fee queries and immutable finance workflows used by the web app. */
public class FeeOperationsDAO {
    private static final Set<String> ADJUSTMENT_TYPES = Set.of(
            "REFUND", "WAIVER", "DEBIT_ADJUSTMENT", "CREDIT_ADJUSTMENT", "REVERSAL");

    public Map<String, Object> getSummary() {
        String sql = "SELECT COALESCE(SUM(sf.total_amount),0) billed, " +
                "COALESCE(SUM(sf.paid_amount),0) collected, " +
                "COALESCE(SUM(CASE WHEN sf.status <> 'PAID' THEN GREATEST(sf.total_amount-sf.paid_amount,0) ELSE 0 END),0) outstanding, " +
                "COALESCE(SUM(CASE WHEN sf.status <> 'PAID' AND sf.due_date < CURRENT_DATE THEN GREATEST(sf.total_amount-sf.paid_amount,0) ELSE 0 END),0) overdue " +
                "FROM student_fees sf";
        Map<String, Object> out = new LinkedHashMap<>();
        try (Connection c = DatabaseConnection.getConnection(); Statement s = c.createStatement(); ResultSet rs = s.executeQuery(sql)) {
            if (rs.next()) {
                double billed = rs.getDouble("billed");
                double collected = rs.getDouble("collected");
                out.put("totalBilled", billed);
                out.put("collected", collected);
                out.put("outstanding", rs.getDouble("outstanding"));
                out.put("overdue", rs.getDouble("overdue"));
                out.put("collectionRate", billed > 0 ? collected * 100d / billed : 0d);
            }
            addTransactionTotal(c, out, "WAIVER", "waived");
            addTransactionTotal(c, out, "REFUND", "refunded");
        } catch (SQLException e) {
            Logger.error("Failed to load fee summary", e);
        }
        return out;
    }

    private void addTransactionTotal(Connection c, Map<String, Object> out, String type, String key) throws SQLException {
        try (PreparedStatement p = c.prepareStatement("SELECT COALESCE(SUM(amount),0) FROM fee_transactions WHERE type=?")) {
            p.setString(1, type);
            try (ResultSet rs = p.executeQuery()) { out.put(key, rs.next() ? rs.getDouble(1) : 0d); }
        }
    }

    public Map<String, Object> searchFees(Map<String, String> q) {
        int page = positiveInt(q.get("page"), 1);
        int size = Math.min(100, positiveInt(q.get("size"), 25));
        List<Object> args = new ArrayList<>();
        StringBuilder where = new StringBuilder(" WHERE 1=1 ");
        addLike(where, args, "search", q.get("search"), "(LOWER(s.name) LIKE ? OR LOWER(COALESCE(s.enrollment_id,u.username,'')) LIKE ?)", 2);
        addEqual(where, args, "sf.status", q.get("status"));
        addNumberEqual(where, args, "sf.category_id", q.get("categoryId"));
        addEqual(where, args, "sf.academic_year", q.get("academicYear"));
        addEqual(where, args, "s.department", q.get("department"));
        addNumberEqual(where, args, "s.semester", q.get("semester"));
        if (q.get("hostelite") != null && !q.get("hostelite").isBlank()) { where.append(" AND s.is_hostelite=?"); args.add(Boolean.parseBoolean(q.get("hostelite"))); }
        if (q.get("dueFrom") != null && !q.get("dueFrom").isBlank()) { try { where.append(" AND sf.due_date>=?"); args.add(java.sql.Date.valueOf(q.get("dueFrom"))); } catch (IllegalArgumentException ignored) {} }
        if (q.get("dueTo") != null && !q.get("dueTo").isBlank()) { try { where.append(" AND sf.due_date<=?"); args.add(java.sql.Date.valueOf(q.get("dueTo"))); } catch (IllegalArgumentException ignored) {} }
        if ("true".equalsIgnoreCase(q.get("overdue"))) where.append(" AND sf.status <> 'PAID' AND sf.due_date < CURRENT_DATE");

        String from = " FROM student_fees sf JOIN students s ON s.id=sf.student_id " +
                "LEFT JOIN users u ON u.id=s.user_id JOIN fee_categories fc ON fc.id=sf.category_id";
        List<Map<String, Object>> rows = new ArrayList<>();
        long total = 0;
        try (Connection c = DatabaseConnection.getConnection()) {
            try (PreparedStatement p = c.prepareStatement("SELECT COUNT(*)" + from + where)) {
                bind(p, args); try (ResultSet rs = p.executeQuery()) { if (rs.next()) total = rs.getLong(1); }
            }
            String sql = "SELECT sf.*,s.name student_name,COALESCE(s.enrollment_id,u.username) student_username," +
                    "s.department,s.semester,fc.category_name" + from + where +
                    " ORDER BY CASE WHEN sf.status <> 'PAID' AND sf.due_date < CURRENT_DATE THEN 0 ELSE 1 END,sf.due_date ASC,sf.id DESC LIMIT ? OFFSET ?";
            try (PreparedStatement p = c.prepareStatement(sql)) {
                bind(p, args); p.setInt(args.size()+1, size); p.setInt(args.size()+2, (page-1)*size);
                try (ResultSet rs = p.executeQuery()) { while (rs.next()) rows.add(mapFee(rs)); }
            }
        } catch (SQLException e) { Logger.error("Failed to search fees", e); }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("data", rows); out.put("page", page); out.put("size", size); out.put("total", total);
        out.put("totalPages", (long)Math.ceil(total/(double)size));
        return out;
    }

    private Map<String, Object> mapFee(ResultSet rs) throws SQLException {
        Map<String, Object> r = new LinkedHashMap<>();
        double total = rs.getDouble("total_amount"), paid = rs.getDouble("paid_amount");
        java.sql.Date due = rs.getDate("due_date");
        r.put("id", rs.getInt("id")); r.put("studentId", rs.getInt("student_id"));
        r.put("studentName", rs.getString("student_name")); r.put("studentUsername", rs.getString("student_username"));
        r.put("department", rs.getString("department")); r.put("semester", rs.getInt("semester"));
        r.put("categoryId", rs.getInt("category_id")); r.put("categoryName", rs.getString("category_name"));
        r.put("academicYear", rs.getString("academic_year")); r.put("totalAmount", total); r.put("paidAmount", paid);
        r.put("balanceAmount", Math.max(0, total-paid)); r.put("dueDate", due); r.put("status", rs.getString("status"));
        r.put("overdue", due != null && due.toLocalDate().isBefore(LocalDate.now()) && paid < total);
        return r;
    }

    public List<Map<String, Object>> getTransactions(int studentId, Integer feeId) {
        List<Map<String, Object>> rows = new ArrayList<>();
        String sql = "SELECT ft.*,u.username created_by_name FROM fee_transactions ft LEFT JOIN users u ON u.id=ft.created_by WHERE ft.student_id=?" +
                (feeId == null ? "" : " AND ft.student_fee_id=?") + " ORDER BY ft.transaction_date DESC,ft.id DESC";
        try (Connection c=DatabaseConnection.getConnection(); PreparedStatement p=c.prepareStatement(sql)) {
            p.setInt(1,studentId); if(feeId!=null)p.setInt(2,feeId);
            try(ResultSet rs=p.executeQuery()){while(rs.next())rows.add(mapTransaction(rs));}
        } catch(SQLException e){Logger.error("Failed to load fee ledger",e);} return rows;
    }

    public Integer getFeeStudentId(int feeId) {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement("SELECT student_id FROM student_fees WHERE id=?")) {
            p.setInt(1, feeId);
            try (ResultSet rs = p.executeQuery()) { return rs.next() ? rs.getInt(1) : null; }
        } catch (SQLException e) { Logger.error("Failed to resolve fee owner", e); return null; }
    }

    public boolean recordPaymentTransaction(Connection c, int studentId, int feeId, Integer paymentId,
            double amount, String mode, int userId, String receipt) throws SQLException {
        String sql="INSERT INTO fee_transactions(transaction_id,student_id,student_fee_id,fee_payment_id,amount,type,description,payment_mode,created_by) VALUES(?,?,?,?,?,'PAYMENT',?,?,?)";
        try(PreparedStatement p=c.prepareStatement(sql)){p.setString(1,receipt);p.setInt(2,studentId);p.setInt(3,feeId);
            if(paymentId==null)p.setNull(4,Types.INTEGER);else p.setInt(4,paymentId);p.setDouble(5,amount);
            p.setString(6,"Payment received");p.setString(7,mode);p.setInt(8,userId);return p.executeUpdate()>0;}
    }

    public Map<String,Object> postAdjustment(int feeId,String type,double amount,String reason,int userId,Integer parentId){
        Map<String,Object> result=new LinkedHashMap<>(); type=type==null?"":type.toUpperCase(Locale.ROOT);
        if(!ADJUSTMENT_TYPES.contains(type)||amount<=0||reason==null||reason.trim().isEmpty()){result.put("error","Valid type, positive amount and reason are required");return result;}
        try(Connection c=DatabaseConnection.getConnection()){c.setAutoCommit(false);try{
            int studentId;double total,paid;try(PreparedStatement p=c.prepareStatement("SELECT student_id,total_amount,paid_amount FROM student_fees WHERE id=? FOR UPDATE")){p.setInt(1,feeId);try(ResultSet rs=p.executeQuery()){if(!rs.next()){result.put("error","Unknown fee entry");c.rollback();return result;}studentId=rs.getInt(1);total=rs.getDouble(2);paid=rs.getDouble(3);}}
            double max=type.equals("REFUND")?paid:type.equals("WAIVER")||type.equals("CREDIT_ADJUSTMENT")?Math.max(0,total-paid):Double.MAX_VALUE;
            if(amount>max){result.put("error","Amount exceeds the allowed balance");c.rollback();return result;}
            String ref="FTX-"+UUID.randomUUID().toString().substring(0,12).toUpperCase(Locale.ROOT);
            try(PreparedStatement p=c.prepareStatement("INSERT INTO fee_transactions(transaction_id,student_id,student_fee_id,parent_transaction_id,amount,type,description,created_by) VALUES(?,?,?,?,?,?,?,?)")){p.setString(1,ref);p.setInt(2,studentId);p.setInt(3,feeId);if(parentId==null)p.setNull(4,Types.INTEGER);else p.setInt(4,parentId);p.setDouble(5,amount);p.setString(6,type);p.setString(7,reason.trim());p.setInt(8,userId);p.executeUpdate();}
            if(type.equals("DEBIT_ADJUSTMENT"))total+=amount;else if(type.equals("WAIVER")||type.equals("CREDIT_ADJUSTMENT"))total-=amount;else if(type.equals("REFUND"))paid-=amount;
            String status=paid>=total?"PAID":paid>0?"PARTIAL":"PENDING";
            try(PreparedStatement p=c.prepareStatement("UPDATE student_fees SET total_amount=?,paid_amount=?,status=? WHERE id=?")){p.setDouble(1,total);p.setDouble(2,paid);p.setString(3,status);p.setInt(4,feeId);p.executeUpdate();}
            c.commit();result.put("transactionId",ref);result.put("status",status);return result;
        }catch(Exception e){c.rollback();throw e;}finally{c.setAutoCommit(true);}}
        catch(Exception e){Logger.error("Failed to post fee adjustment",e);result.put("error","Failed to post adjustment");return result;}
    }

    public Map<String,Object> createPaymentRequest(int userId,int feeId,double amount,String mode,String reference,java.sql.Date paymentDate,String note){
        Map<String,Object> out=new LinkedHashMap<>();
        String sql="INSERT INTO fee_payment_requests(student_fee_id,student_id,amount,payment_mode,reference_number,payment_date,student_note) " +
                "SELECT sf.id,s.id,?,?,?,?,? FROM student_fees sf JOIN students s ON s.id=sf.student_id WHERE sf.id=? AND s.user_id=? AND sf.status<>'PAID' AND ?<=sf.total_amount-sf.paid_amount";
        try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)){
            p.setDouble(1,amount);p.setString(2,mode);p.setString(3,reference);p.setDate(4,paymentDate);p.setString(5,note);p.setInt(6,feeId);p.setInt(7,userId);p.setDouble(8,amount);
            if(p.executeUpdate()==0){out.put("error","Fee entry was not found, is already paid, or the amount exceeds its balance");return out;}try(ResultSet rs=p.getGeneratedKeys()){if(rs.next())out.put("id",rs.getInt(1));}out.put("status","PENDING");
        }catch(SQLException e){out.put("error",e.getMessage()!=null&&e.getMessage().toLowerCase().contains("unique")?"This payment reference was already submitted":"Failed to submit payment request");}return out;
    }

    public List<Map<String,Object>> getPaymentRequests(Integer studentId,String status){
        List<Map<String,Object>> rows=new ArrayList<>();StringBuilder sql=new StringBuilder("SELECT pr.*,s.name student_name,COALESCE(s.enrollment_id,u.username) student_username,fc.category_name FROM fee_payment_requests pr JOIN students s ON s.id=pr.student_id LEFT JOIN users u ON u.id=s.user_id JOIN student_fees sf ON sf.id=pr.student_fee_id JOIN fee_categories fc ON fc.id=sf.category_id WHERE 1=1");
        if(studentId!=null)sql.append(" AND pr.student_id=?");if(status!=null&&!status.isBlank())sql.append(" AND pr.status=?");sql.append(" ORDER BY pr.created_at DESC");
        try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement(sql.toString())){int i=1;if(studentId!=null)p.setInt(i++,studentId);if(status!=null&&!status.isBlank())p.setString(i,status.toUpperCase(Locale.ROOT));try(ResultSet rs=p.executeQuery()){while(rs.next()){Map<String,Object> r=new LinkedHashMap<>();r.put("id",rs.getInt("id"));r.put("studentFeeId",rs.getInt("student_fee_id"));r.put("studentId",rs.getInt("student_id"));r.put("studentName",rs.getString("student_name"));r.put("studentUsername",rs.getString("student_username"));r.put("categoryName",rs.getString("category_name"));r.put("amount",rs.getDouble("amount"));r.put("paymentMode",rs.getString("payment_mode"));r.put("referenceNumber",rs.getString("reference_number"));r.put("paymentDate",rs.getDate("payment_date"));r.put("status",rs.getString("status"));r.put("studentNote",rs.getString("student_note"));r.put("reviewNote",rs.getString("review_note"));r.put("createdAt",rs.getTimestamp("created_at"));rows.add(r);}}}catch(SQLException e){Logger.error("Failed to load payment requests",e);}return rows;
    }

    public boolean reviewPaymentRequest(int requestId,String status,String note,int reviewer,EnhancedFeeDAO feeDAO){
        status=status==null?"":status.toUpperCase(Locale.ROOT);if(!status.equals("APPROVED")&&!status.equals("REJECTED"))return false;
        try(Connection c=DatabaseConnection.getConnection()){c.setAutoCommit(false);try{
            int feeId;double amount;String mode,reference,current;try(PreparedStatement p=c.prepareStatement("SELECT student_fee_id,amount,payment_mode,reference_number,status FROM fee_payment_requests WHERE id=? FOR UPDATE")){p.setInt(1,requestId);try(ResultSet rs=p.executeQuery()){if(!rs.next()){c.rollback();return false;}feeId=rs.getInt(1);amount=rs.getDouble(2);mode=rs.getString(3);reference=rs.getString(4);current=rs.getString(5);}}
            if(!"PENDING".equals(current)){c.rollback();return false;}
            // Claim the request before posting so concurrent reviewers cannot create duplicate payments.
            try(PreparedStatement p=c.prepareStatement("UPDATE fee_payment_requests SET status='PROCESSING',reviewed_by=? WHERE id=? AND status='PENDING'")){p.setInt(1,reviewer);p.setInt(2,requestId);if(p.executeUpdate()!=1){c.rollback();return false;}}c.commit();
            if(status.equals("APPROVED")){FeePayment payment=new FeePayment();payment.setStudentFeeId(feeId);payment.setAmount(amount);payment.setPaymentMode(mode);payment.setTransactionId(reference);payment.setRemarks("Approved payment request #"+requestId+(note==null?"":" - "+note));payment.setReceivedBy(reviewer);payment.setPaymentDate(new java.util.Date());EnhancedFeeDAO.PaymentResult pr=feeDAO.recordPaymentDetailed(payment);if(!pr.ok){try(PreparedStatement p=c.prepareStatement("UPDATE fee_payment_requests SET status='PENDING',reviewed_by=NULL WHERE id=?")){p.setInt(1,requestId);p.executeUpdate();}return false;}}
            try(PreparedStatement p=c.prepareStatement("UPDATE fee_payment_requests SET status=?,review_note=?,reviewed_by=?,reviewed_at=CURRENT_TIMESTAMP WHERE id=? AND status='PROCESSING'")){p.setString(1,status);p.setString(2,note);p.setInt(3,reviewer);p.setInt(4,requestId);return p.executeUpdate()==1;}
        }catch(Exception e){c.rollback();throw e;}finally{c.setAutoCommit(true);}}catch(Exception e){Logger.error("Failed to review payment request",e);return false;}
    }

    public Map<String,Object> bulkAssign(Map<String,Object> body,int userId,boolean preview){
        Map<String,Object> out=new LinkedHashMap<>();int category=number(body.get("categoryId"),0);double amount=decimal(body.get("amount"),0);String year=text(body.get("academicYear"));String source=text(body.get("sourceType"));
        if(category<=0||amount<=0||year.isBlank()){out.put("error","categoryId, academicYear and a positive amount are required");return out;}
        List<Integer> ids=new ArrayList<>();Object raw=body.get("studentIds");if(raw instanceof List<?> list)for(Object x:list){int id=number(x,0);if(id>0)ids.add(id);} 
        try(Connection c=DatabaseConnection.getConnection()){
            if(ids.isEmpty()){StringBuilder sql=new StringBuilder("SELECT id FROM students WHERE 1=1");List<Object>a=new ArrayList<>();addFilter(sql,a,"department",body.get("department"));addFilter(sql,a,"specialization",body.get("specialization"));addFilter(sql,a,"batch",body.get("batch"));if(body.get("semester")!=null&&!text(body.get("semester")).isBlank()){sql.append(" AND semester=?");a.add(number(body.get("semester"),0));}if(body.get("hostelite")!=null&&!text(body.get("hostelite")).isBlank()){sql.append(" AND is_hostelite=?");a.add(Boolean.parseBoolean(text(body.get("hostelite"))));}try(PreparedStatement p=c.prepareStatement(sql.toString())){bind(p,a);try(ResultSet rs=p.executeQuery()){while(rs.next())ids.add(rs.getInt(1));}}}
            List<Integer> eligible=new ArrayList<>(),duplicates=new ArrayList<>();try(PreparedStatement p=c.prepareStatement("SELECT 1 FROM student_fees WHERE student_id=? AND category_id=? AND academic_year=?")){for(int id:ids){p.setInt(1,id);p.setInt(2,category);p.setString(3,year);try(ResultSet rs=p.executeQuery()){(rs.next()?duplicates:eligible).add(id);}}}
            out.put("matched",ids.size());out.put("eligible",eligible.size());out.put("duplicates",duplicates.size());out.put("duplicateStudentIds",duplicates);if(preview)return out;
            c.setAutoCommit(false);try{java.sql.Date due=parseDate(body.get("dueDate"));try(PreparedStatement p=c.prepareStatement("INSERT INTO student_fees(student_id,category_id,academic_year,total_amount,due_date) VALUES(?,?,?,?,?)")){for(int id:eligible){p.setInt(1,id);p.setInt(2,category);p.setString(3,year);p.setDouble(4,amount);if(due==null)p.setNull(5,Types.DATE);else p.setDate(5,due);p.addBatch();}p.executeBatch();}String ref="BATCH-"+UUID.randomUUID().toString().substring(0,8).toUpperCase(Locale.ROOT);try(PreparedStatement p=c.prepareStatement("INSERT INTO fee_assignment_batches(batch_reference,source_type,criteria_json,category_id,academic_year,amount,due_date,assigned_count,skipped_count,created_by) VALUES(?,?,?,?,?,?,?,?,?,?)")){p.setString(1,ref);p.setString(2,source.isBlank()?"COHORT":source);p.setString(3,body.toString());p.setInt(4,category);p.setString(5,year);p.setDouble(6,amount);if(due==null)p.setNull(7,Types.DATE);else p.setDate(7,due);p.setInt(8,eligible.size());p.setInt(9,duplicates.size());p.setInt(10,userId);p.executeUpdate();}c.commit();out.put("batchReference",ref);out.put("assigned",eligible.size());out.put("skipped",duplicates.size());return out;}catch(Exception e){c.rollback();throw e;}finally{c.setAutoCommit(true);}
        }catch(Exception e){Logger.error("Bulk fee assignment failed",e);out.put("error","Bulk assignment failed");return out;}
    }

    public int createReminders(int userId,int daysAhead){String sql="INSERT INTO fee_reminders(student_fee_id,student_id,recipient_user_id,reminder_type,message,due_date,created_by) SELECT sf.id,s.id,s.user_id,CASE WHEN sf.due_date<CURRENT_DATE THEN 'OVERDUE' ELSE 'UPCOMING' END,'Fee payment reminder: ' || fc.category_name || ' has an outstanding balance.',sf.due_date,? FROM student_fees sf JOIN students s ON s.id=sf.student_id JOIN fee_categories fc ON fc.id=sf.category_id WHERE sf.status<>'PAID' AND sf.due_date<=? AND NOT EXISTS(SELECT 1 FROM fee_reminders fr WHERE fr.student_fee_id=sf.id AND CAST(fr.created_at AS DATE)=CURRENT_DATE)";try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){p.setInt(1,userId);p.setDate(2,java.sql.Date.valueOf(LocalDate.now().plusDays(Math.max(0,daysAhead))));return p.executeUpdate();}catch(SQLException e){Logger.error("Failed to create fee reminders",e);return -1;}}
    public List<Map<String,Object>> getReminders(int userId,boolean all){List<Map<String,Object>>rows=new ArrayList<>();String sql="SELECT fr.*,s.name student_name,fc.category_name FROM fee_reminders fr JOIN students s ON s.id=fr.student_id JOIN student_fees sf ON sf.id=fr.student_fee_id JOIN fee_categories fc ON fc.id=sf.category_id"+(all?"":" WHERE fr.recipient_user_id=?")+" ORDER BY fr.created_at DESC";try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){if(!all)p.setInt(1,userId);try(ResultSet rs=p.executeQuery()){while(rs.next()){Map<String,Object>r=new LinkedHashMap<>();r.put("id",rs.getInt("id"));r.put("studentFeeId",rs.getInt("student_fee_id"));r.put("studentName",rs.getString("student_name"));r.put("categoryName",rs.getString("category_name"));r.put("type",rs.getString("reminder_type"));r.put("message",rs.getString("message"));r.put("dueDate",rs.getDate("due_date"));r.put("readAt",rs.getTimestamp("read_at"));r.put("createdAt",rs.getTimestamp("created_at"));rows.add(r);}}}catch(SQLException e){Logger.error("Failed to load reminders",e);}return rows;}

    private Map<String,Object> mapTransaction(ResultSet rs)throws SQLException{Map<String,Object>r=new LinkedHashMap<>();r.put("id",rs.getInt("id"));r.put("transactionId",rs.getString("transaction_id"));r.put("studentId",rs.getInt("student_id"));r.put("studentFeeId",rs.getObject("student_fee_id"));r.put("amount",rs.getDouble("amount"));r.put("type",rs.getString("type"));r.put("description",rs.getString("description"));r.put("paymentMode",rs.getString("payment_mode"));r.put("transactionDate",rs.getTimestamp("transaction_date"));r.put("createdByName",rs.getString("created_by_name"));return r;}
    private static void addLike(StringBuilder w,List<Object>a,String key,String v,String clause,int repeats){if(v!=null&&!v.isBlank()){w.append(" AND ").append(clause);for(int i=0;i<repeats;i++)a.add("%"+v.toLowerCase(Locale.ROOT)+"%");}}
    private static void addEqual(StringBuilder w,List<Object>a,String column,String v){if(v!=null&&!v.isBlank()){w.append(" AND ").append(column).append("=?");a.add(v);}}
    private static void addNumberEqual(StringBuilder w,List<Object>a,String column,String v){if(v!=null&&!v.isBlank()){try{w.append(" AND ").append(column).append("=?");a.add(Integer.parseInt(v));}catch(NumberFormatException ignored){}}}
    private static void addFilter(StringBuilder s,List<Object>a,String column,Object value){if(value!=null&&!text(value).isBlank()){s.append(" AND ").append(column).append("=?");a.add(value);}}
    private static void bind(PreparedStatement p,List<Object>a)throws SQLException{for(int i=0;i<a.size();i++)p.setObject(i+1,a.get(i));}
    private static int positiveInt(String v,int d){try{int n=Integer.parseInt(v);return n>0?n:d;}catch(Exception e){return d;}}
    private static int number(Object v,int d){try{return v instanceof Number?((Number)v).intValue():Integer.parseInt(text(v));}catch(Exception e){return d;}}
    private static double decimal(Object v,double d){try{return v instanceof Number?((Number)v).doubleValue():Double.parseDouble(text(v));}catch(Exception e){return d;}}
    private static String text(Object v){return v==null?"":String.valueOf(v).trim();}
    private static java.sql.Date parseDate(Object v){try{return text(v).isBlank()?null:java.sql.Date.valueOf(text(v));}catch(Exception e){return null;}}
}
