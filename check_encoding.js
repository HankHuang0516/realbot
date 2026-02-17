const fs = require('fs');
const path = require('path');

function check(dir) {
  fs.readdirSync(dir, { withFileTypes: true }).forEach(f => {
    const p = path.join(dir, f.name);
    if (f.isDirectory()) check(p);
    else if (f.name.endsWith('.js') || f.name.endsWith('.md') || f.name.endsWith('.json')) {
      const buf = fs.readFileSync(p);
      const str = buf.toString('utf8');
      const re = Buffer.from(str, 'utf8');
      if (buf.length !== re.length) {
        console.log('INVALID:', p, '(orig:', buf.length, 'vs re-encoded:', re.length, ')');
      }
    }
  });
}

check('backend');
// Also check root test files
['test_demo_flow.js', 'test_anim_limbs.js', 'test_text_wrap.js', 'BUG-FIXES-SUMMARY.md'].forEach(f => {
  if (fs.existsSync(f)) {
    const buf = fs.readFileSync(f);
    const str = buf.toString('utf8');
    const re = Buffer.from(str, 'utf8');
    if (buf.length !== re.length) {
      console.log('INVALID:', f, '(orig:', buf.length, 'vs re-encoded:', re.length, ')');
    }
  }
});

console.log('Done checking.');
