const data = [
    { studentId: 1, courseId: 1, date: "2026-02-24", status: "PRESENT" },
    { studentId: 2, courseId: 1, date: "2026-02-24", status: "ABSENT" }
];

async function seed() {
    for (const record of data) {
        const res = await fetch('http://localhost:7000/api/attendance', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(record)
        });
        console.log(`Seeded student ${record.studentId}: ${res.status}`);
    }
}

seed();
