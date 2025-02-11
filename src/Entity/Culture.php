<?php

namespace App\Entity;

use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Validator\Constraints as Assert;

#[ORM\Entity(repositoryClass: 'App\Repository\CultureRepository')]
class Culture
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(type: 'integer')]
    private ?int $id = null;

    #[ORM\Column(type: 'string', length: 100)]
    #[Assert\NotBlank(message: "Le nom de la culture est requis.")]
    #[Assert\Length(min: 3, max: 100, minMessage: "Le nom de la culture doit contenir au moins {{ limit }} caractères.", maxMessage: "Le nom de la culture ne doit pas dépasser {{ limit }} caractères.")]
    private ?string $nomCulture = null;

    #[ORM\Column(type: 'date', nullable: true)] 
    #[Assert\NotBlank(message: "La date de semis est requise.")]
    #[Assert\Type(type: "\DateTimeInterface", message: "La date de semis doit être valide.")]
    private ?\DateTimeInterface $dateSemis = null;

    #[ORM\Column(type: 'integer')]
    #[Assert\NotBlank(message: "La durée est requise.")]
    #[Assert\GreaterThan(value: 0, message: "La durée doit être supérieure à zéro.")]
    #[Assert\Type(type: "integer", message: "La durée doit être un nombre entier.")]
    private ?int $duree = null;

    #[ORM\ManyToOne(targetEntity: 'App\Entity\Parcelle',cascade: ["persist"])]
    #[ORM\JoinColumn(nullable: false)]
    #[Assert\NotBlank(message: "La parcelle est requise.")]
    private ?Parcelle $parcelle = null;

    #[ORM\Column(type: 'string', length: 50)]
    #[Assert\NotBlank(message: "Le statut de la culture est requis.")]
    #[Assert\Choice(
        choices: ['en_culture', 'recolte', 'termine'],
        message: "Le statut doit être l'un des suivants : en_culture, recolte, termine."
    )]
    private ?string $statut;


    public function getId(): ?int
    {
        return $this->id;
    }

    public function getNomCulture(): ?string
    {
        return $this->nomCulture;
    }

    public function setNomCulture(string $nomCulture): self
    {
        $this->nomCulture = $nomCulture;

        return $this;
    }

    public function getDateSemis(): ?\DateTimeInterface
    {
        return $this->dateSemis;
    }

    public function setDateSemis(\DateTimeInterface $dateSemis): self
    {
        $this->dateSemis = $dateSemis;

        return $this;
    }

    public function getDuree(): ?int
    {
        return $this->duree;
    }

    public function setDuree(int $duree): self
    {
        $this->duree = $duree;

        return $this;
    }

    public function getParcelle(): ?Parcelle
    {
        return $this->parcelle;
    }

    public function setParcelle(Parcelle $parcelle): self
    {
        $this->parcelle = $parcelle;

        return $this;
    }

    public function getStatut(): ?string
    {
        return $this->statut;
    }

    public function setStatut(string $statut): self
    {
        $this->statut = $statut;

        return $this;
    }

    public static function getStatutlChoices(): array
    {
        return [
            'En culture' => 'en_culture',
            'Récolte' => 'recolte',
            'Terminé' => 'termine',
        ];
    }
}

