# Root AWS Credentials Used for CI Deploys

GitHub Actions deploys this stack using the AWS account's root credentials
(`arn:aws:iam::446034593894:root`), not a scoped IAM user.

## Current Evidence

- `.github/workflows/ci.yaml`'s `deploy` job takes `AWS_ACCESS_KEY_ID` /
  `AWS_SECRET_ACCESS_KEY` from GitHub secrets and hands them straight to
  `serverless deploy`.
- The credentials populated there are the account root's, by explicit
  choice made when scaffolding Slice 1 — a scoped IAM user was offered and
  declined for now.

## Why This Is a Problem

Root credentials have unlimited permissions across the entire AWS account
— billing, IAM, every service, every region. A leaked GitHub secret, a
compromised Actions run, or a misconfigured step has no blast-radius limit
if it's root. `serverless deploy` only needs CloudFormation, Lambda, API
Gateway, IAM role creation for the function's execution role, and
CloudWatch Logs — a small, enumerable set.

## Implications

- Any secret leak (log line, malicious dependency in the Actions run,
  compromised GitHub Action) compromises the entire AWS account, not just
  this Lambda.
- Root credentials can't be individually revoked/rotated without touching
  every other use of the account.
- Acceptable for now only because this is a single-service personal
  project with nothing else of value in the account yet.

## Improvement Proposal

Create an IAM user scoped to a policy covering just: `cloudformation:*` on
the `biwenger-client-production` stack, `lambda:*` /
`apigateway:*` on this service's resources, `iam:CreateRole` /
`iam:PassRole` limited to the function's execution role, and
`logs:*` on its log group. Swap the GitHub secrets to that user's
credentials; no code change needed elsewhere.

## Migration Approach

Do this before any second service/credential is added to the account, or
before the Biwenger credentials themselves are considered sensitive enough
to warrant it — whichever comes first.
