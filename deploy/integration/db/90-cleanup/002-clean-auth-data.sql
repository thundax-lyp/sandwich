SET NAMES utf8mb4;

-- Clean integration-test persistent auth data only.
-- Access tokens, refresh tokens, auth sessions, pre-auth sessions, captcha values
-- and OpenAPI nonces are Redis/cache data and are cleaned by IntegrationRedisCleaner.

DELETE FROM `auth_principal_login_event`
WHERE `id` LIKE 'it-%'
   OR `client_id` LIKE 'it-%'
   OR (`principal_type` = 'USER'
       AND `principal_id` BETWEEN 9100000000000000100 AND 9100000000000000999)
   OR (`principal_type` = 'MEMBER'
       AND `principal_id` BETWEEN 9100000000000060000 AND 9100000000000060999)
   OR (`principal_type` = 'OPEN_CLIENT'
       AND `principal_id` BETWEEN 9100000000000050000 AND 9100000000000050999);

DELETE FROM `auth_oauth_authorization`
WHERE `authorization_code` LIKE 'it-%'
   OR `client_id` LIKE 'it-%'
   OR (`principal_type` = 'USER'
       AND `principal_id` BETWEEN 9100000000000000100 AND 9100000000000000999)
   OR (`principal_type` = 'MEMBER'
       AND `principal_id` BETWEEN 9100000000000060000 AND 9100000000000060999)
   OR (`principal_type` = 'OPEN_CLIENT'
       AND `principal_id` BETWEEN 9100000000000050000 AND 9100000000000050999);

DELETE FROM `auth_principal_credential`
WHERE `id` BETWEEN 9100000000000010200 AND 9100000000000010999
   OR `id` BETWEEN 9100000000000050200 AND 9100000000000050999
   OR `id` BETWEEN 9100000000000060200 AND 9100000000000060999
   OR `identity_id` BETWEEN 9100000000000010100 AND 9100000000000010999
   OR `identity_id` BETWEEN 9100000000000050100 AND 9100000000000050999
   OR `identity_id` BETWEEN 9100000000000060100 AND 9100000000000060999
   OR (`principal_type` = 'USER'
       AND `principal_id` BETWEEN 9100000000000000100 AND 9100000000000000999)
   OR (`principal_type` = 'MEMBER'
       AND `principal_id` BETWEEN 9100000000000060000 AND 9100000000000060999)
   OR (`principal_type` = 'OPEN_CLIENT'
       AND `principal_id` BETWEEN 9100000000000050000 AND 9100000000000050999);

DELETE FROM `auth_principal_identity`
WHERE `id` BETWEEN 9100000000000010100 AND 9100000000000010999
   OR `id` BETWEEN 9100000000000050100 AND 9100000000000050999
   OR `id` BETWEEN 9100000000000060100 AND 9100000000000060999
   OR `identity_value` LIKE 'it-%'
   OR `identity_value` LIKE '%@sandwish.local'
   OR `identity_value` = '15500006666'
   OR (`principal_type` = 'USER'
       AND `principal_id` BETWEEN 9100000000000000100 AND 9100000000000000999)
   OR (`principal_type` = 'MEMBER'
       AND `principal_id` BETWEEN 9100000000000060000 AND 9100000000000060999)
   OR (`principal_type` = 'OPEN_CLIENT'
       AND `principal_id` BETWEEN 9100000000000050000 AND 9100000000000050999);
