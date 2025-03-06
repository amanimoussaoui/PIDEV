<?php

namespace App\Entity;

use App\Repository\ParcelleRepository;
use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Validator\Constraints as Assert;
use Doctrine\Common\Collections\ArrayCollection;
use Doctrine\Common\Collections\Collection;

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
    
    #[ORM\Column(type: 'float', nullable: true)]
private ?float $latitude = null;

#[ORM\Column(type: 'float', nullable: true)]
private ?float $longitude = null;

#[ORM\Column(type: 'json', nullable: true)]
private ?array $boundary = null;

#[ORM\ManyToOne(targetEntity: Terrain::class, inversedBy: 'parcelles')]
#[ORM\JoinColumn(nullable: false)]
#[Assert\NotNull(message: "Le terrain est obligatoire.")]
private ?Terrain $terrain = null;


#[ORM\ManyToOne(targetEntity: Utilisateurs::class, inversedBy: 'parcelles')]
#[ORM\JoinColumn(name: 'utilisateur_id', referencedColumnName: 'id', nullable: true)]
private ?Utilisateurs $utilisateur = null;

#[ORM\Column(type: 'string', length: 255, nullable: true)]
private ?string $mapImage = null;

#[ORM\OneToMany(targetEntity: Culture::class, mappedBy: 'parcelle')]
    private Collection $cultures;

   

public function getMapImage(): ?string
{
    return $this->mapImage;
}

public function setMapImage(?string $mapImage): self
{
    $this->mapImage = $mapImage;
    return $this;
}

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

    public function getLatitude(): ?float
    {
        return $this->latitude;
    }
    
    public function setLatitude(?float $latitude): self
    {
        $this->latitude = $latitude;
        return $this;
    }
    
    public function getLongitude(): ?float
    {
        return $this->longitude;
    }
    
    public function setLongitude(?float $longitude): self
    {
        $this->longitude = $longitude;
        return $this;
    }

    public function getBoundary(): ?array
{
    return $this->boundary;
}

public function setBoundary(?array $boundary): self
{
    $this->boundary = $boundary;
    return $this;
}

public function getUtilisateur(): ?Utilisateurs
{
    return $this->utilisateur;
}

public function setUtilisateur(?Utilisateurs $utilisateur): static
{
    $this->utilisateur = $utilisateur;
    return $this;
}


public function getTerrain(): ?Terrain
{
    return $this->terrain;
}

public function setTerrain(?Terrain $terrain): self
{
    $this->terrain = $terrain;
    return $this;
}

    public static function getTypeSolChoices(): array
{
    return [
        'Argileux' => 'argileux',
        'Sableux' => 'sableux',
        'Limoneux' => 'limoneux',
        'Humifère' => 'humifère',
    ];
}

public function __construct()
{
    $this->cultures = new ArrayCollection();
}

/**
 * @return Collection<int, Culture>
 */
public function getCultures(): Collection
{
    return $this->cultures;
}

public function addCulture(Culture $culture): self
{
    if (!$this->cultures->contains($culture)) {
        $this->cultures[] = $culture;
        $culture->setParcelle($this);
    }

    return $this;
}


}
