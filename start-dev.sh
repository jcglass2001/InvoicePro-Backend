echo "Using environment file: .env.dev"
cat .env.dev

ENV_FILE=./.env.dev docker-compose --env-file ./.env.dev --verbose up -d --build
#ENV_FILE=./.env.dev docker-compose --verbose up -d --build