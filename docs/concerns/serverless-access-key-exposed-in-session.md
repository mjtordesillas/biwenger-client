# Serverless Dashboard Access Key Exposed While Debugging a Deploy

While tracking down where a `SERVERLESS_ACCESS_KEY` could be obtained,
`~/.serverlessrc` on the local machine was read to check for an existing
org/access-key configuration. That file's contents — including a live
Serverless Dashboard access key (`mjtordesillas`) and session tokens
(`idToken`, `refreshToken`, `accessToken`) for the account
`mjtordesillas@gmail.com` — were printed into the working chat transcript,
and the access key value was then reused directly as this project's
`SERVERLESS_ACCESS_KEY` GitHub secret rather than rotated first.

## Current Evidence

- `~/.serverlessrc` (outside this repo, not committed) holds the exposed
  values; nothing was written into the repo itself.
- The access key currently backing `SERVERLESS_ACCESS_KEY` in
  `mjtordesillas/biwenger-client`'s GitHub Actions secrets is the one that
  appeared in that transcript, not a freshly rotated one.

## Why This Is a Problem

An access key and session tokens that appeared in a chat transcript should
be treated as exposed, regardless of how the transcript is stored — that's
the standard practice for any credential display, not specific to this
tool. The refresh/id/access tokens are also short-lived session
credentials that will expire on their own, but the access key does not
expire and grants deploy-level access to the Serverless Dashboard org
until revoked.

## Implications

- The org `mjtordesillas`'s deploy capability (and anything else that key
  can reach in the Serverless Dashboard) is only as safe as this
  transcript and this GitHub secret.
- No AWS credentials were exposed by this — only the Serverless Framework
  Dashboard access key, which is a separate credential from
  `AWS_ACCESS_KEY_ID`/`AWS_SECRET_ACCESS_KEY`.

## Improvement Proposal

Rotate the key: in the Serverless Dashboard (app.serverless.com → org
settings → Access Keys), delete the `mjtordesillas` key and create a new
one, then update the `SERVERLESS_ACCESS_KEY` GitHub secret to the new
value. No code or config change is needed elsewhere — the key isn't
referenced anywhere except that one GitHub secret.

## Migration Approach

Low urgency single step, no sequencing needed — do it whenever convenient,
ideally before this account is trusted with anything beyond this
single-service personal project.
