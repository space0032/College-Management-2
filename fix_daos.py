#!/usr/bin/env python3
"""
Script to batch replace printStackTrace() calls with Logger.error() in DAO files
"""

import os
import re

def fix_dao_file(filepath):
    """Fix a single DAO file"""
    with open(filepath, 'r') as f:
        content = f.read()
    
    # Check if Logger is already imported
    if 'import com.college.utils.Logger;' not in content:
        # Add Logger import after DatabaseConnection import
        content = content.replace(
            'import com.college.utils.DatabaseConnection;',
            'import com.college.utils.DatabaseConnection;\nimport com.college.utils.Logger;'
        )
    
    # Replace printStackTrace patterns
    # Pattern 1: Simple printStackTrace in catch block
    content = re.sub(
        r'(\s+)e\.printStackTrace\(\);',
        lambda m: f'{m.group(1)}Logger.error("Database operation failed", e);',
        content
    )
    
    # Write back
    with open(filepath, 'w') as f:
        f.write(content)
    
    return True

def main():
    dao_dir = 'src/main/java/com/college/dao'
    fixed_files = []
    
    for filename in os.listdir(dao_dir):
        if filename.endswith('.java'):
            filepath = os.path.join(dao_dir, filename)
            if fix_dao_file(filepath):
                fixed_files.append(filename)
                print(f"Fixed: {filename}")
    
    print(f"\nTotal files fixed: {len(fixed_files)}")

if __name__ == '__main__':
    main()
