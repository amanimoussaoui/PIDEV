<?php

namespace App\Entity;

use App\Repository\ParcelleRepository;
use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Validator\Constraints as Assert;

#[ORM\Entity(repositoryClass: ParcelleRepository::class)]
class Parcelle
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\Column(length: 255, nullable: true)]
    #[Assert\NotBlank(message: "Le nom est requis.")]
    private ?string $nom = null;

    #[ORM\Column(nullable: true)]
    #[Assert\NotBlank(message: "La superficie est requise.")]
    #[Assert\Type(type: 'float', message: "La superficie doit être un nombre.")]
    #[Assert\GreaterThan(value: 0, message: "La superficie doit être un nombre positif.")]
    private ?float $superficie = null;

    #[ORM\Column(length: 255, nullable: true)]
    #[Assert\NotBlank(message: "La localisation est requise.")]
    #[Assert\Length(max: 255, maxMessage: "La localisation ne peut pas dépasser 255 caractères.")]
    private ?string $localisation = null;

    #[ORM\Column(type: 'string', length: 20, nullable: true)]
    #[Assert\NotBlank(message: "Le type de sol est requis.")]
    #[Assert\Choice(
        choices: [
            'Argileux' => 'argileux',
            'Sableux' => 'sableux',
            'Limoneux' => 'limoneux',
            'Humifère' => 'humifère',
        ], 
        message: "Choisissez un type de sol valide (Argileux, Sableux, Limoneux, Humifère)."
    )]
    private ?string $typeSol = null;
    

    #[ORM\Column(nullable: true)]
    private ?int $utilisateurId = null;

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getNom(): ?string
    {
        return $this->nom;
    }

    public function setNom(?string $nom): static
    {
        $this->nom = $nom;
        return $this;
    }

    public function getSuperficie(): ?float
    {
        return $this->superficie;
    }

    public function setSuperficie(?float $superficie): static
    {
        $this->superficie = $superficie;
        return $this;
    }

    public function getLocalisation(): ?string
    {
        return $this->localisation;
    }

    public function setLocalisation(?string $localisation): static
    {
        $this->localisation = $localisation;
        return $this;
    }

    public function getTypeSol(): ?string
    {
        return $this->typeSol;
    }

    public function setTypeSol(?string $typeSol): self
    {
        $this->typeSol = $typeSol;
        return $this;
    }

    public function getUtilisateurId(): ?int
    {
        return $this->utilisateurId;
    }

    public function setUtilisateurId(?int $utilisateurId): static
    {
        $this->utilisateurId = $utilisateurId;
        return $this;
    }
}
