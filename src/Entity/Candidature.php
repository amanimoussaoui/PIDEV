<?php

namespace App\Entity;

use App\Repository\CandidatureRepository;
use App\Repository\TerrainRepository;
use Doctrine\DBAL\Types\Types;
use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Validator\Context\ExecutionContextInterface;
use Symfony\Component\Validator\Constraints as Assert;
use App\Service\OpenAIService;

#[ORM\Entity(repositoryClass: CandidatureRepository::class)]

class Candidature
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\Column(type: Types::DATE_MUTABLE)]
    private ?\DateTimeInterface $dateDebut=null ;


    #[ORM\Column(type: Types::DATE_MUTABLE)]
    private ?\DateTimeInterface $dateFin=null ;

    #[ORM\Column(length: 255)]
    private ?string $but = null;

    #[ORM\Column]
    private ?float $montant = null;


    #[ORM\ManyToOne(inversedBy: 'candidatures')]
    private ?Terrain $idTerrain = null;

    #[ORM\ManyToOne(inversedBy: 'candidatures')]
    private ?Utilisateurs $utilisateur = null;

    #[ORM\Column(type: "string", length: 255, nullable: false)]
private string $etat = 'en attente';

#[ORM\Column(type: 'text', nullable: true)]
private $recommandation;


    #[Assert\Callback]
    public function validateDates(ExecutionContextInterface $context): void
    {
        if ($this->dateDebut && $this->dateFin) {
            if ($this->dateFin <= $this->dateDebut) {
                $context->buildViolation('La date de fin doit être strictement supérieure à la date de début.')
                    ->atPath('dateFin')
                    ->addViolation();
            }
        }
    }


    public function getId(): ?int
    {
        return $this->id;
    }

    public function getDateDebut(): ?\DateTimeInterface
    {
        return $this->dateDebut;
    }

    public function setDateDebut(?\DateTimeInterface $dateDebut): self
{
    if ($dateDebut === null) {
        $dateDebut = new \DateTime(); // Valeur par défaut
    }
    $this->dateDebut = $dateDebut;
    return $this;
}


    public function getDateFin(): ?\DateTimeInterface
    {
        return $this->dateFin;
    }

    public function setDateFin(?\DateTimeInterface $dateFin): self
    {
        if ($dateFin === null) {
            $dateFin = new \DateTime();
        }
        $this->dateFin = $dateFin;
        return $this;
    }

    public function getBut(): ?string
    {
        return $this->but;
    }

    public function setBut(string $but): static
    {
        $this->but = $but;

        return $this;
    }


    public function getMontant(): ?float
    {
        return $this->montant;
    }

    public function setMontant(float $montant): static
    {
        $this->montant = $montant;

        return $this;
    }


    public function getIdTerrain(): ?Terrain
{
    return $this->idTerrain;
}

public function setIdTerrain(?Terrain $idTerrain): self
{
    $this->idTerrain = $idTerrain;
    return $this;
}
    

    public function validateFields(): array
{
    $errors = [];
    if (empty($this->dateDebut)) $errors[] = 'La date de début est obligatoire.';
    if (empty($this->dateFin)) $errors[] = 'La date de fin est obligatoire.';
    if ($this->dateFin < $this->dateDebut) $errors[] = 'La date de fin doit être supérieure à la date de début.';
    if (empty($this->montant)) $errors[] = 'Le montant est obligatoire.';
    if (empty($this->but)) $errors[] = 'Le but est obligatoire.';
    if (empty($this->idTerrain)) $errors[] = 'Le terrain est obligatoire.';
    return $errors;
}

    public function getUtilisateur(): ?Utilisateurs
    {
        return $this->utilisateur;
    }

    public function setUtilisateur(?Utilisateurs $utilisateur): self
    {
        $this->utilisateur = $utilisateur;

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

public function getRecommandation(): ?string
{
    return $this->recommandation;
}

public function setRecommandation(?string $recommandation): self
{
    $this->recommandation = $recommandation;
    return $this;
}




}