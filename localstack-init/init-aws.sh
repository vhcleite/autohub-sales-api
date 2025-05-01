#!/bin/bash

# Script para inicializar recursos AWS no LocalStack automaticamente.

echo "########### Criando recursos SNS/SQS no LocalStack ###########"

REGION="us-east-1"

# Nomes dos recursos (consistente com application-local.yml)
SNS_TOPIC_NAME="AutoHubBusinessEventsTopic-local"
SALES_QUEUE_NAME="SalesApi_Events_Queue-local"
SALES_DLQ_NAME="SalesApi_Events_DLQ-local"

echo "--- Criando Tópico SNS: ${SNS_TOPIC_NAME} ---"
awslocal sns create-topic --name ${SNS_TOPIC_NAME} --region ${REGION}
SNS_TOPIC_ARN=$(awslocal sns list-topics --query "Topics[?ends_with(TopicArn, ':${SNS_TOPIC_NAME}')].TopicArn" --output text --region ${REGION})
echo "SNS Topic ARN: ${SNS_TOPIC_ARN}"

echo "--- Criando DLQ SQS: ${SALES_DLQ_NAME} ---"
awslocal sqs create-queue --queue-name ${SALES_DLQ_NAME} --region ${REGION}
SALES_DLQ_URL=$(awslocal sqs get-queue-url --queue-name ${SALES_DLQ_NAME} --query QueueUrl --output text --region ${REGION})
SALES_DLQ_ARN=$(awslocal sqs get-queue-attributes --queue-url ${SALES_DLQ_URL} --attribute-names QueueArn --query Attributes.QueueArn --output text --region ${REGION})
echo "Sales DLQ ARN: ${SALES_DLQ_ARN}"

echo "--- Criando Fila SQS Principal: ${SALES_QUEUE_NAME} ---"
awslocal sqs create-queue --queue-name ${SALES_QUEUE_NAME} --region ${REGION} \
  --attributes '{
    "RedrivePolicy": "{\"deadLetterTargetArn\":\"'"${SALES_DLQ_ARN}"'\",\"maxReceiveCount\":\"3\"}",
    "VisibilityTimeout": "100"
  }'
SALES_QUEUE_URL=$(awslocal sqs get-queue-url --queue-name ${SALES_QUEUE_NAME} --query QueueUrl --output text --region ${REGION})
SALES_QUEUE_ARN=$(awslocal sqs get-queue-attributes --queue-url ${SALES_QUEUE_URL} --attribute-names QueueArn --query Attributes.QueueArn --output text --region ${REGION})
echo "Sales Queue ARN: ${SALES_QUEUE_ARN}"


echo "--- Criando Assinatura SNS -> SQS (${SNS_TOPIC_NAME} -> ${SALES_QUEUE_NAME}) ---"
awslocal sns subscribe \
  --topic-arn ${SNS_TOPIC_ARN} \
  --protocol sqs \
  --notification-endpoint ${SALES_QUEUE_ARN} \
  --attributes '{ "RawMessageDelivery": "true" }' \
  --region ${REGION}

echo "########### Criação de recursos SNS/SQS concluída. ###########"

