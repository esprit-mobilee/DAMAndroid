
import os
import re

def resolve_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # Pattern captures: HEAD content (1), Demande content (2)
    pattern = re.compile(r'<<<<<<< HEAD\n(.*?)\n=======\n(.*?)\n>>>>>>> [^\n]*\n', re.DOTALL)

    def replace_func(match):
        head_content = match.group(1)
        other_content = match.group(2)
        
        # 1. Strict equality or whitespace equality -> Pick HEAD (doesn't matter)
        if head_content.strip() == other_content.strip():
            return head_content + '\n'

        # 2. Check packages
        # Naive extraction of package line
        head_pkg = re.search(r'package\s+([\w\.]+)', head_content)
        other_pkg = re.search(r'package\s+([\w\.]+)', other_content)
        
        target_pkg = "com.example.esprit"
        
        # If both present and match target_pkg, pick OTHER (Demande/Feature)
        if head_pkg and target_pkg in head_pkg.group(1) and other_pkg and target_pkg in other_pkg.group(1):
             return other_content + '\n'
             
        # If Other has 'com.esprit.connect', we need to port it.
        # Take other_content, replace 'com.esprit.connect' with 'com.example.esprit'
        # Also ensure the package declaration itself is fixed.
        if 'com.esprit.connect' in other_content:
             ported_content = other_content.replace('com.esprit.connect', 'com.example.esprit')
             return ported_content + '\n'
             
        # Default fallback: If we can't decide, use OTHER (Demande) but formatted?
        # Actually for files like TeacherHomeScreen, even if package lines weren't in the conflict block (but here entire file is conflict usually),
        # we want to prefer Demande.
        
        # If the conflict block DOES NOT contain package declaration (e.g. inside a class),
        # We usually prefer Demande (Feature branch).
        # Let's verify if 'com.esprit.connect' is involved.
        if 'com.esprit.connect' in other_content:
             return other_content.replace('com.esprit.connect', 'com.example.esprit') + '\n'
             
        # Blindly prefer Demande for everything else?
        # User wants "Merge Demande and TS". Demande is likely the source of truth for new features.
        return other_content + '\n'

    new_content = pattern.sub(replace_func, content)
    
    if new_content != content:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(new_content)
        return True
    return False

def main():
    root_dir = r'C:\Users\LENOVO\OneDrive - ESPRIT\Bureau\DAM android - Copie\DAMAndroid\app\src\main\java'
    # Exclude files we manually fix? No, script won't touch them if no conflict markers.
    
    resolved_count = 0
    remaining_conflicts = []

    for root, dirs, files in os.walk(root_dir):
        for file in files:
            if file.endswith('.kt') or file.endswith('.java'):
                filepath = os.path.join(root, file)
                try:
                    resolve_file(filepath)
                    with open(filepath, 'r', encoding='utf-8') as f:
                        if '<<<<<<< HEAD' in f.read():
                            remaining_conflicts.append(filepath)
                        else:
                            resolved_count += 1
                except Exception as e:
                    print(f"Error processing {filepath}: {e}")

    print(f"Resolved conflicts in {resolved_count} files.")

if __name__ == '__main__':
    main()
