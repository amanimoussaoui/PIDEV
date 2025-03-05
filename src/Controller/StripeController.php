<?php

namespace App\Controller;

use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;
use Symfony\Component\HttpFoundation\Request;
use Stripe;
use Symfony\Contracts\HttpClient\HttpClientInterface; // Inclure HttpClient si tu veux utiliser une API externe

class StripeController extends AbstractController
{
    #[Route('/stripe', name: 'app_stripe')]
    public function index(): Response
    {
        return $this->render('stripe/index.html.twig', [
            'stripe_public_key' => $_ENV["STRIPE_PUBLIC_KEY"],
        ]);
    }

    #[Route('/stripe/create-charge', name: 'app_stripe_charge', methods: ['POST'])]
    public function createCharge(Request $request, HttpClientInterface $client)
    {
        Stripe\Stripe::setApiKey($_ENV["STRIPE_SECRET_KEY"]);
        
        try {
            $charge = Stripe\Charge::create([
                "amount" => 5 * 100, // Montant en centimes
                "currency" => "usd",
                "source" => $request->request->get('stripeToken'),
                "description" => "Test de paiement Binaryboxtuts"
            ]);

            // Envoi du SMS après un paiement réussi (tu peux utiliser une API comme Twilio)
            $this->sendSMSNotification(); // Méthode pour envoyer un SMS

            $this->addFlash('success', 'Paiement réussi !');

        } catch (\Exception $e) {
            $this->addFlash('error', 'Échec du paiement : ' . $e->getMessage());
        }

        return $this->redirectToRoute('app_stripe', [], Response::HTTP_SEE_OTHER);
    }

    // Fonction pour envoyer un SMS après le paiement
    private function sendSMSNotification()
    {
        // Exemple avec Twilio, tu peux utiliser ton propre service SMS

        $twilioClient = new \Twilio\Rest\Client($_ENV['TWILIO_SID'], $_ENV['TWILIO_AUTH_TOKEN']);
        $twilioClient->messages->create(
            '+21695921917', // Numéro du destinataire
            [
                'from' => $_ENV['TWILIO_PHONE_NUMBER'], // Ton numéro Twilio
                'body' => 'Agriwise:Le paiement a été effectué avec succès. Merci pour votre achat !'
            ]
        );
    }
}
