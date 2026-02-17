/**
 * Flickr OAuth 1.0a Authorization Script
 *
 * Usage:
 *   node flickr-auth.js <API_KEY> <API_SECRET>
 *
 * Steps:
 *   1. Run this script with your API key and secret
 *   2. Open the URL it prints in your browser
 *   3. Authorize the app on Flickr
 *   4. Copy the verifier code from the URL (oauth_verifier=XXX)
 *   5. Paste it back into this script
 *   6. It will print your OAUTH_TOKEN and OAUTH_TOKEN_SECRET
 */

const Flickr = require('flickr-sdk');
const readline = require('readline');

const API_KEY = process.argv[2];
const API_SECRET = process.argv[3];

if (!API_KEY || !API_SECRET) {
  console.log('Usage: node flickr-auth.js <API_KEY> <API_SECRET>');
  process.exit(1);
}

const rl = readline.createInterface({
  input: process.stdin,
  output: process.stdout
});

async function main() {
  try {
    // Step 1: Get request token
    const oauth = new Flickr.OAuth(API_KEY, API_SECRET);

    // Use oob (out-of-band) callback so Flickr shows the verifier code on screen
    const res = await oauth.request('oob');
    const requestToken = res.body.oauth_token;
    const requestTokenSecret = res.body.oauth_token_secret;

    // Step 2: Generate authorization URL
    const authUrl = oauth.authorizeUrl(requestToken, 'write');

    console.log('\n========================================');
    console.log('Step 1: Open this URL in your browser:');
    console.log('========================================');
    console.log(authUrl);
    console.log('\nStep 2: Authorize the app, then you will see a 9-digit verifier code.');
    console.log('');

    // Step 3: Wait for user to input verifier
    rl.question('Step 3: Paste the verifier code here: ', async (verifier) => {
      try {
        // Step 4: Exchange for access token
        const verifyRes = await oauth.verify(requestToken, verifier.trim(), requestTokenSecret);

        const oauthToken = verifyRes.body.oauth_token;
        const oauthTokenSecret = verifyRes.body.oauth_token_secret;
        const fullname = verifyRes.body.fullname;
        const username = verifyRes.body.username;

        console.log('\n========================================');
        console.log('SUCCESS! Authorized as:', fullname || username);
        console.log('========================================');
        console.log('\nAdd these to your Railway environment variables:\n');
        console.log(`FLICKR_API_KEY=${API_KEY}`);
        console.log(`FLICKR_API_SECRET=${API_SECRET}`);
        console.log(`FLICKR_OAUTH_TOKEN=${oauthToken}`);
        console.log(`FLICKR_OAUTH_TOKEN_SECRET=${oauthTokenSecret}`);
        console.log('\n========================================');
      } catch (err) {
        console.error('Failed to verify:', err.message);
      }
      rl.close();
    });
  } catch (err) {
    console.error('Failed to get request token:', err.message);
    rl.close();
  }
}

main();
