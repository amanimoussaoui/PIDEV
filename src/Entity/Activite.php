<?php

namespace App\Entity;

use App\Repository\ActiviteRepository;
use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Validator\Constraints as Assert;

#[ORM\Entity(repositoryClass: ActiviteRepository::class)]
class Activite
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(type: 'integer')]
    private ?int $id = null;

    #[ORM\Column(type: 'text')]
    #[Assert\NotBlank(message: "La description de l'activité est requise.")]
    private ?string $description = null;

    #[ORM\Column(type: 'string', length: 50)]
    #[Assert\Choice(
        choices: ['Semis', 'Arrosage', 'Fertilisation', 'Récolte', 'Traitement', 'Désherbage'],
        message: "Le type d'activité doit être l'un des suivants : Semis, Arrosage, Fertilisation, Récolte, Traitement, Désherbage."
    )]
    private ?string $type = null;

    #[ORM\Column(type: 'date')]
    #[Assert\NotBlank(message: "La date de l'activité est requise.")]
    #[Assert\Type(type: "\DateTimeInterface", message: "La date de l'activité doit être valide.")]
    private ?\DateTimeInterface $date = null;

    #[ORM\ManyToOne(targetEntity: Parcelle::class,cascade: ["persist"])]
    #[ORM\JoinColumn(nullable: true)]
    private ?Parcelle $parcelle = null;

    #[ORM\ManyToOne(targetEntity: Culture::class,cascade: ["persist"])]
    #[ORM\JoinColumn(nullable: true)]
    private ?Culture $culture = null;

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getDescription(): ?string
    {
        return $this->description;
    }

    public function setDescription(string $description): self
    {
        $this->description = $description;

        return $this;
    }

    public function getType(): ?string
    {
        return $this->type;
    }

    public function setType(string $type): self
    {
        $this->type = $type;

        return $this;
    }

    public function getDate(): ?\DateTimeInterface
    {
        return $this->date;
    }

    public function setDate(\DateTimeInterface $date): self
    {
        $this->date = $date;

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

    public function getCulture(): ?Culture
    {
        return $this->culture;
    }

    public function setCulture(?Culture $culture): self
    {
        $this->culture = $culture;

        return $this;
    }
}
