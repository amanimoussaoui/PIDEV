<?php

namespace App\Service;

use Symfony\Contracts\HttpClient\HttpClientInterface;
use Psr\Cache\CacheItemPoolInterface;

class MeteoService
{
    private $client;
    private $apiKey;
    private $cache;

    public function __construct(HttpClientInterface $client, string $apiKey, CacheItemPoolInterface $cache)
    {
        $this->client = $client;
        $this->apiKey = $apiKey;
        $this->cache = $cache;
    }

    public function getWeatherData(float $latitude, float $longitude): array
    {
        $cacheKey = 'weather_' . $latitude . '_' . $longitude;
        $cacheItem = $this->cache->getItem($cacheKey);

        // If cache exists, return it
        if ($cacheItem->isHit()) {
            return $cacheItem->get();
        }

        // Otherwise, fetch new data
        $url = sprintf(
            'https://api.openweathermap.org/data/2.5/weather?lat=%s&lon=%s&appid=%s&units=metric',
            $latitude,
            $longitude,
            $this->apiKey
        );

        try {
            $response = $this->client->request('GET', $url);
            $weatherData = $response->toArray();

            // Cache the response for 30 minutes
            $cacheItem->set($weatherData);
            $cacheItem->expiresAfter(1800);
            $this->cache->save($cacheItem);

            return $weatherData;
        } catch (\Exception $e) {
            return ['error' => 'Unable to fetch weather data'];
        }
    }
}