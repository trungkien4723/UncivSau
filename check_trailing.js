const fs = require('fs');
const path = 'D:/lamviec/test/uncivSAU/android/assets/jsons/Civ VI/Civics.json';
let content = fs.readFileSync(path, 'utf8');

// Find trailing commas
let inString = false;
let escape = false;
for (let i = 0; i < content.length - 1; i++) {
  const c = content[i];
  if (inString) {
    if (escape) escape = false;
    else if (c === '\\') escape = true;
    else if (c === '"') inString = false;
    continue;
  }
  if (c === '"') inString = true;
  else if (c === ',' && content[i+1] === ']' || c === ',' && content[i+1] === '}') {
    console.log('Trailing comma at', i, 'context:', content.substring(i-30, i+10));
  }
}
console.log('Done');