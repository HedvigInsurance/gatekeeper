#!/usr/bin/env bash
# Push only if it's not a pull request
if [ -z "$TRAVIS_PULL_REQUEST" ] || [ "$TRAVIS_PULL_REQUEST" == "false" ]; then
  # Push only if we're testing the master branch
  if [ "$TRAVIS_BRANCH" == "master" ]; then

    # This is needed to login on AWS and push the image on ECR
    # Change it accordingly to your docker repo
    pip install --user awscli || exit 1
    export PATH=$PATH:$HOME/.local/bin
    eval $(aws ecr get-login --no-include-email --region ${AWS_DEFAULT_REGION}) || exit 1

    # Build and push
    docker build -t $IMAGE_NAME .  || exit 1
    echo "Pushing $IMAGE_NAME:latest"
    docker tag $IMAGE_NAME:latest $REMOTE_IMAGE_URL:${TRAVIS_COMMIT} || exit 1
    docker push $REMOTE_IMAGE_URL:${TRAVIS_COMMIT} || exit 1
    echo "Pushed $IMAGE_NAME:${TRAVIS_COMMIT}"
  else
    echo "Skipping deploy because branch is not 'master'"
  fi
else
  echo "Skipping deploy because it's a pull request"
fi
