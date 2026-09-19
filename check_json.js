const fs = require('fs');
const path = 'D:/lamviec/test/uncivSAU/android/assets/jsons/Civ VI/Civics.json';
let content = fs.readFileSync(path, 'utf8');

let depth = 0;
let inString = false;
let escape = false;
for (let i = 0; i < content.length; i++) {
  const c = content[i];
  if (inString) {
    if (escape) escape = false;
    else if (c === '\\') escape = true;
    else if (c === '"') inString = false;
    continue;
  }
  if (c === '"') inString = true;
  else if (c === '[') depth++;
  else if (c === ']') depth--;
  else if (c === '{') depth++;
  else if (c === '}') depth--;
  
  if (depth < 0) {
    console.log('Unbalanced ] at', i, 'context:', content.substring(i-20, i+20));
    break;
  }
}
console.log('Final depth:', depth);