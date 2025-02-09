<?php

namespace App\Entity;

use App\Repository\RecolteRepository;
use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Validator\Constraints as Assert;

#[ORM\Entity(repositoryClass: RecolteRepository::class)]
class Recolte
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\Column(type: "date")]
    #[Assert\NotBlank(message: "La date de récolte est requise.")]
    #[Assert\Type(type: "\DateTimeInterface", message: "La date de récolte doit être valide.")]
    private ?\DateTimeInterface $dateRecolte = null;

    #[ORM\Column(type: "float")]
    #[Assert\NotBlank(message: "La quantité est requise.")]
    #[Assert\Positive(message: "La quantité doit être un nombre positif.")]
    private ?float $quantite = null;

    #[ORM\Column(length: 50)]
    #[Assert\NotBlank(message: "La qualité est requise.")]
    #[Assert\Choice(choices: ["Excellente", "Bonne", "Moyenne", "Médiocre", "Mauvaise"], message: "Choisissez une qualité valide.")]
    private ?string $qualite = null;

    #[ORM\Column(type: "float")]
    #[Assert\NotBlank(message: "Le prix unitaire est requis.")]
    #[Assert\Positive(message: "Le prix unitaire doit être un nombre positif.")]
    private ?float $prixUnitaire = null;

    #[ORM\ManyToOne(targetEntity: Culture::class)]
    #[ORM\JoinColumn(nullable: false)]
    #[Assert\NotBlank(message: "La culture est requise.")]
    private ?Culture $culture = null;

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getDateRecolte(): ?\DateTimeInterface
    {
        return $this->dateRecolte;
    }

    public function setDateRecolte(?\DateTimeInterface $dateRecolte): static
    {
        $this->dateRecolte = $dateRecolte;
        return $this;
    }

    public function getQuantite(): ?float
    {
        return $this->quantite;
    }

    public function setQuantite(?float $quantite): static
    {
        $this->quantite = $quantite;
        return $this;
    }

    public function getQualite(): ?string
    {
        return $this->qualite;
    }

    public function setQualite(string $qualite): static
    {
        $this->qualite = $qualite;
        return $this;
    }

    public function getPrixUnitaire(): ?float
    {
        return $this->prixUnitaire;
    }

    public function setPrixUnitaire(?float $prixUnitaire): static
    {
        $this->prixUnitaire = $prixUnitaire;
        return $this;
    }

    public function getCulture(): ?Culture
    {
        return $this->culture;
    }

    public function setCulture(?Culture $culture): static
    {
        $this->culture = $culture;
        return $this;
    }
}