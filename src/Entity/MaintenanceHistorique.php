<?php

namespace App\Entity;

use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Validator\Constraints as Assert;
use App\Entity\Machine;

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
    
    #[ORM\Column(type: "datetime")] // Changement ici
    #[Assert\NotNull(message: "La date de maintenance est requise.")]
    private ?\DateTime $dateMaintenance = null; // Changement ici

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

    public function getDateMaintenance(): ?\DateTime // Changement ici
    {
        return $this->dateMaintenance;
    }

    public function setDateMaintenance(\DateTime $dateMaintenance): static // Changement ici
    {
        $this->dateMaintenance = $dateMaintenance;
        return $this;
    }
}
