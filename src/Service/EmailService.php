<?php

namespace App\Service;

use Symfony\Component\Mailer\MailerInterface;
use Symfony\Component\Mime\Email;
use Symfony\Bridge\Twig\Mime\TemplatedEmail;

class EmailService
{
    private $mailer;

    public function __construct(MailerInterface $mailer)
    {
        $this->mailer = $mailer;
    }

    public function sendEmail(string $to, string $subject, string $body): void
    {
        $email = (new Email())
            ->from('tasnimsdiri2001@gmail.com') // Remplacez par votre adresse e-mail
            ->to($to)
            ->subject($subject)
            ->html($body);

        $this->mailer->send($email);
    }
}