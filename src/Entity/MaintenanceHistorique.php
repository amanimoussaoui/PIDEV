<?php

namespace App\Entity;

use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Validator\Constraints as Assert;

#[ORM\Entity]
class MaintenanceHistorique
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\ManyToOne(targetEntity: Machine::class)]
    #[ORM\JoinColumn(nullable: false, onDelete: "CASCADE")]
    private ?Machine $machine = null;

    #[ORM\Column(type: "datetime")]
    #[Assert\NotNull(message: "La date de maintenance est requise.")]
    private ?\DateTimeInterface $dateMaintenance = null;

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getMachine(): ?Machine
    {
        return $this->machine;
    }

    public function setMachine(?Machine $machine): static
    {
        $this->machine = $machine;
        return $this;
    }

    public function getDateMaintenance(): ?\DateTimeInterface
    {
        return $this->dateMaintenance;
    }

    public function setDateMaintenance(\DateTimeInterface $dateMaintenance): static
    {
        $this->dateMaintenance = $dateMaintenance;
        return $this;
    }
}
