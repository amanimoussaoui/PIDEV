<?php
namespace App\Service;

use Twilio\Rest\Client;
use Symfony\Component\HttpFoundation\JsonResponse;

use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\Routing\Annotation\Route;

class SmsService
{
    private Client $client;
    private string $twilioNumber;

    public function __construct(string $twilioSid, string $twilioToken, string $twilioNumber)
    {
        $this->client = new Client($twilioSid, $twilioToken);
        $this->twilioNumber = $twilioNumber;
    }


    public function envoyerSms(): JsonResponse
    {
        // Récupérer les informations de Twilio depuis les paramètres
        $sid = $_ENV['TWILIO_SID'];
        $authToken = $_ENV['TWILIO_AUTH_TOKEN'];
        $twilioPhoneNumber = $_ENV['TWILIO_PHONE_NUMBER'];

        // Créer une instance de Twilio Client
        $client = new Client($sid, $authToken);

        // Numéro de destination
        $to = "+21695921917";  // Remplace par le numéro que tu veux

        // Envoi du message SMS
        $message = $client->messages->create(
            $to = "+21695921917",// Numéro de destination
            [
                'from' => $twilioPhoneNumber, // Numéro Twilio
                'body' => 'Agriwise : Bienvenue cher client, votre commande est confirmée. Merci pour votre achat !'
            ]
        );

        // Retourner une réponse JSON pour signaler le succès
       return new JsonResponse(['status' => 'success', 'message' => 'SMS envoyé avec succès!']);
    }
}