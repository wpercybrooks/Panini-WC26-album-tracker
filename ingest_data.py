import re
import sqlite3
import os

def parse_master_list(file_path):
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()

    stickers = []

    # Parse FWC
    fwc_matches = re.findall(r'\| (FWC \d+) \| ([^|]+) \| ([^|]+) \|', content)
    for code, name, detail in fwc_matches:
        stickers.append({
            'id': code.strip(),
            'name': name.strip(),
            'country': 'FIFA',
            'group': 'Intro/Museum'
        })

    # Parse CC
    cc_matches = re.findall(r'\| (CC \d+) \| ([^|]+) \| ([^|]+) \|', content)
    for code, name, team in cc_matches:
        stickers.append({
            'id': code.strip(),
            'name': name.strip(),
            'country': team.strip(),
            'group': 'Coca-Cola'
        })

    # Parse National Teams
    # Look for Group headers
    groups = re.split(r'### (Group [A-L])', content)
    for i in range(1, len(groups), 2):
        group_name = groups[i]
        group_content = groups[i+1]
        
        # Look for Nation headers in Group content
        nations = re.split(r'#### ([^(]+) \(([A-Z]{3})\)', group_content)
        for j in range(1, len(nations), 3):
            nation_name = nations[j].strip()
            nation_code = nations[j+1].strip()
            nation_stickers_text = nations[j+2].strip()
            
            # Extract individual stickers
            # Pattern: [CODE] [NUMBER] [NAME]
            # e.g. MEX 1 (Badge), MEX 2 Luis Malagón
            sticker_matches = re.findall(rf'({nation_code} \d+) (.*?)(?=\s*,\s*{nation_code}|\s*\.\s*(?:$|\n))', nation_stickers_text)
            for code, name in sticker_matches:
                stickers.append({
                    'id': code.strip(),
                    'name': name.strip(),
                    'country': nation_name,
                    'group': group_name
                })

    return stickers

def create_db(stickers, db_path):
    if os.path.exists(db_path):
        os.remove(db_path)
    
    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()
    
    cursor.execute('''
        CREATE TABLE stickers (
            id TEXT PRIMARY KEY NOT NULL,
            name TEXT,
            country TEXT,
            `group` TEXT,
            ncopies INTEGER NOT NULL DEFAULT 0
        )
    ''')
    
    for s in stickers:
        cursor.execute(
            'INSERT INTO stickers (id, name, country, `group`, ncopies) VALUES (?, ?, ?, ?, ?)',
            (s['id'], s['name'], s['country'], s['group'], 0)
        )
    
    conn.commit()
    conn.close()

if __name__ == "__main__":
    stickers = parse_master_list('master_list.md')
    print(f"Parsed {len(stickers)} stickers")
    
    os.makedirs('app/src/main/assets', exist_ok=True)
    create_db(stickers, 'app/src/main/assets/catalog.db')
    print("Database created at app/src/main/assets/catalog.db")
