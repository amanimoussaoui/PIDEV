<?php

namespace App\Service;

use GuzzleHttp\Client;
use GuzzleHttp\Exception\RequestException;
use Psr\Log\LoggerInterface;

class OpenAIService
{
    private $client;
    private $apiKey;
    private $logger;

    public function __construct(string $apiKey, LoggerInterface $logger)
    {
        $this->client = new Client();
        $this->apiKey = $apiKey;
        $this->logger = $logger;
    }

    public function generateText(string $prompt): ?string
    {
        try {
            $url = 'https://api.openai.com/v1/chat/completions';

            $requestData = [
                'model' => 'gpt-3.5-turbo',
                'messages' => [
                    ['role' => 'system', 'content' => 'You are a helpful assistant.'],
                    ['role' => 'user', 'content' => $prompt],
                ],
                'max_tokens' => 150,
            ];

            // Log the request data
            $this->logger->info('OpenAI API Request:', $requestData);

            $response = $this->client->post($url, [
                'json' => $requestData,
                'headers' => [
                    'Authorization' => 'Bearer ' . $this->apiKey,
                ],
            ]);

            $data = json_decode($response->getBody()->getContents(), true);

            // Log the response data
            $this->logger->info('OpenAI API Response:', $data);

            return $data['choices'][0]['message']['content'] ?? null;

        } catch (RequestException $e) {
            // Log the error
            $this->logger->error('OpenAI API Error: ' . $e->getMessage());
            return null;
        }
    }

    public function getRecommandation(string $input): ?string
    {
        $prompt = "Provide a recommendation based on the input: " . $input;
        return $this->generateText($prompt);
    }
}