const fs = require('fs');
const path = 'D:/lamviec/test/uncivSAU/android/assets/jsons/Civ VI/Civics.json';

let content = fs.readFileSync(path, 'utf8');

// Remove comments more carefully - handle both // and /* */
content = content.replace(/\/\*[\s\S]*?\*\//g, '');  // Remove /* */ comments
content = content.replace(/\/\/.*$/gm, '');          // Remove // comments

const civGroups = JSON.parse(content);

// Add column object to all civics
for (const group of civGroups) {
  for (const civic of group.civics) {
    civic.column = { columnNumber: group.columnNumber };
  }
}

fs.writeFileSync(path, JSON.stringify(civGroups, null, 2) + '\n', { encoding: 'utf8' });
console.log('Added column object to all civics');