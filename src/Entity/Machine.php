<?php

namespace App\Entity;

use App\Repository\MachineRepository;
use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Validator\Constraints as Assert;

#[ORM\Entity(repositoryClass: MachineRepository::class)]
class Machine
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\Column(length: 255, nullable: false)]
    #[Assert\NotBlank(message: "Le nom de la machine est requis")]
    private ?string $nom = null;

    #[ORM\Column(length: 255, nullable: false)]
    #[Assert\NotBlank(message: "La description est requise")]
    private ?string $description = null;

    #[ORM\Column(nullable: false)]
    #[Assert\NotNull(message: "Le prix est requis")]
    #[Assert\Positive(message: "Le prix doit être un nombre positif")]
    private ?float $prix = null;

    #[ORM\Column(length: 255, nullable: false)]
    #[Assert\NotBlank(message: "L'état de la machine est requis")]
    private ?string $etat = null;

    #[ORM\Column(length: 255, nullable: false)]
    #[Assert\NotBlank(message: "La disponibilité est requise")]
    private ?string $disponibilite = null;

    #[ORM\Column(type: "datetime", nullable: true)]
    #[Assert\Type("\DateTimeInterface")]
    private ?\DateTimeInterface $date_maintenance = null;

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getNom(): ?string
    {
        return $this->nom;
    }

    public function setNom(string $nom): static
    {
        $this->nom = $nom;
        return $this;
    }

    public function getDescription(): ?string
    {
        return $this->description;
    }

    public function setDescription(string $description): static
    {
        $this->description = $description;
        return $this;
    }

    public function getPrix(): ?float
    {
        return $this->prix;
    }

    public function setPrix(float $prix): static
    {
        $this->prix = $prix;
        return $this;
    }

    public function getEtat(): ?string
    {
        return $this->etat;
    }

    public function setEtat(string $etat): static
    {
        $this->etat = $etat;
        return $this;
    }

    public function getDisponibilite(): ?string
    {
        return $this->disponibilite;
    }

    public function setDisponibilite(string $disponibilite): static
    {
        $this->disponibilite = $disponibilite;
        return $this;
    }

    public function getDateMaintenance(): ?\DateTimeInterface
    {
        return $this->date_maintenance;
    }

    public function setDateMaintenance(?\DateTimeInterface $date_maintenance): static
    {
        $this->date_maintenance = $date_maintenance;
        return $this;
    }
}
