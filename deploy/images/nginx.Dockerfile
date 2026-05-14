ARG SANDWISH_NGINX_IMAGE=nginx:1.27-alpine
FROM ${SANDWISH_NGINX_IMAGE}

COPY deploy/nginx/default.conf /etc/nginx/conf.d/default.conf
COPY sandwish-admin-web/dist /usr/share/nginx/html/admin
