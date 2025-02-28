<?php

namespace App\Service;

use Stripe\Stripe;
use Stripe\Checkout\Session;
use Symfony\Component\DependencyInjection\ParameterBag\ParameterBagInterface;
use App\Entity\Panier;

class StripeService
{
    private string $secretKey;

    public function __construct(ParameterBagInterface $params)
    {
        $this->secretKey = $params->get('stripe_secret_key');
    }

    public function createCheckoutSession(array $panierItems): Session
    {
        Stripe::setApiKey($this->secretKey);

        $lineItems = [];

        foreach ($panierItems as $item) {
            $product = $item->getProduct(); // Assurez-vous que cette relation existe bien dans l'entité Panier
            if (!$product) {
                throw new \Exception("Produit introuvable dans le panier.");
            }

            $lineItems[] = [
                'price_data' => [
                    'currency' => 'eur',
                    'product_data' => [
                        'name' => $product->getNom(),
                    ],
                    'unit_amount' => $product->getPrix() * 100, // Prix en centimes
                ],
                'quantity' => $item->getQuantite(),
            ];
        }

        return Session::create([
            'payment_method_types' => ['card'],
            'line_items' => $lineItems,
            'mode' => 'payment',
            'success_url' => 'http://127.0.0.1:8000/panier/success',
            'cancel_url' => 'http://127.0.0.1:8000/panier/cancel',
        ]);
    }
}
