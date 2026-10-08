// npm install playwright; uses a headless browser, never the user's desktop.
const { chromium } = require('playwright');
const fs = require('fs'), path = require('path');
(async () => {
  const project = path.resolve(__dirname, '../../..');
  const assets = path.join(project, 'src/main/resources/assets/velvet_eclipse');
  const output = path.join(assets, 'textures/gui/city');
  const icons = JSON.parse(fs.readFileSync(path.join(__dirname, 'shop-art.json')));
  const font = fs.readFileSync(path.join(assets, 'font/display.ttf')).toString('base64');
  fs.mkdirSync(output, {recursive: true});
  const browser = await chromium.launch({headless: true, channel: process.env.CITY_BROWSER || 'msedge'});
  try {
    const page = await browser.newPage({viewport: {width: 480, height: 344}});
    for (const [id, svg] of Object.entries(icons)) {
      await page.setContent(`<style>@font-face{font-family:display;src:url(data:font/ttf;base64,${font})}body{margin:0;background:transparent}svg{width:480px;height:344px}</style>${svg}`);
      await page.evaluate(() => document.fonts.ready);
      await page.screenshot({path: path.join(output, id+'.png'), omitBackground: true});
    }
  } finally { await browser.close(); }
})();
