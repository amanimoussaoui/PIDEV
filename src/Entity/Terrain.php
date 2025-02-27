<?php

namespace App\Entity;

use App\Repository\TerrainRepository;
use Doctrine\Common\Collections\ArrayCollection;
use Doctrine\Common\Collections\Collection;
use Doctrine\DBAL\Types\Types;
use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Validator\Constraints as Assert;
use Symfony\Component\Validator\Constraints\NotBlank;

#[ORM\Entity(repositoryClass: TerrainRepository::class)]
class Terrain
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;
    
    
    #[ORM\Column(length: 255)]
#[Assert\NotBlank(message: "La localisation est obligatoire.")]
#[Assert\Length(
    min: 3,
    max: 255,
    minMessage: "La localisation doit comporter au moins {{ limit }} caractères.",
    maxMessage: "La localisation ne peut pas dépasser {{ limit }} caractères."
)]
#[Assert\Regex(
    pattern: "/^[a-zA-ZÀ-ÿ\s]+$/",
    message: "La localisation ne doit contenir que des lettres et des espaces."
)]
private ?string $localisation = null;

    #[ORM\Column]
    #[Assert\NotBlank(message: "La superficie est obligatoire.")]
    #[Assert\Type(
        type: "float",
        message: "La superficie doit être un nombre décimal."
    )]
    private ?float $superficie = null;

    #[ORM\Column]
    #[Assert\NotBlank(message: "Le prix est obligatoire.")]
    #[Assert\Type(
        type: "float",
        message: "Le prix doit être un nombre décimal."
    )]
    private ?float $prix = null;

    #[ORM\Column(type: Types::TEXT)]
    #[Assert\NotBlank(message: "La description est obligatoire.")]
    #[Assert\Length(
        min: 10,
        minMessage: "La description doit comporter au moins {{ limit }} caractères."
    )]
    private ?string $description = null;

    #[ORM\Column(length: 255)]
    #[Assert\Image(
        mimeTypes: ["image/jpeg", "image/png"],
        mimeTypesMessage: "L'image doit être de type JPEG ou PNG."
    )]
    private ?string $image = null;
    


    #[ORM\OneToMany(mappedBy: 'idTerrain', targetEntity: Candidature::class, cascade: ['remove'])]
private Collection $candidatures;

    #[ORM\ManyToOne(inversedBy: 'terrains')]
    #[ORM\JoinColumn(nullable: false)]
    private ?Utilisateurs $utilisateur = null;

    public function __construct()
    {
        $this->candidatures = new ArrayCollection();
    }

    public function validateFields(): array
    {
        $errors = [];
        if (empty($this->localisation)) $errors[] = 'La localisation est obligatoire.';
        if (empty($this->superficie)) $errors[] = 'La superficie est obligatoire.';
        if (empty($this->prix)) $errors[] = 'Le prix est obligatoire.';
        if (empty($this->description)) $errors[] = 'La description est obligatoire.';
        if (empty($this->image)) $errors[] = 'L\'image est obligatoire.';
        return $errors;
    }

    public function getId(): ?int { return $this->id; }
    public function getLocalisation(): ?string { return $this->localisation; }
    public function setLocalisation(string $localisation): static { $this->localisation = $localisation; return $this; }
    public function getSuperficie(): ?float { return $this->superficie; }
    public function setSuperficie(float $superficie): static { $this->superficie = $superficie; return $this; }
    public function getPrix(): ?float { return $this->prix; }
    public function setPrix(float $prix): static { $this->prix = $prix; return $this; }
    public function getDescription(): ?string { return $this->description; }
    public function setDescription(string $description): static { $this->description = $description; return $this; }
    public function getImage(): ?string
{
    return $this->image;
}
    public function setImage(?string $image): self
    {
        $this->image = $image;
        return $this;
    }
    public function getImagePath(): string { return 'uploads/' . $this->image; }
    public function getCandidatures(): Collection { return $this->candidatures; }
    public function getUtilisateur(): ?Utilisateurs
{
    return $this->utilisateur;
}

public function setUtilisateur(?Utilisateurs $utilisateur): self
{
    $this->utilisateur = $utilisateur;
    return $this;
}


public function addCandidature(Candidature $candidature): self
{
    if (!$this->candidatures->contains($candidature)) {
        $this->candidatures->add($candidature);
        $candidature->setIdTerrain($this);
    }
    return $this;
}

public function removeCandidature(Candidature $candidature): static
{
    if ($this->candidatures->removeElement($candidature)) {
        // Défaire la relation côté Candidature
        if ($candidature->getIdTerrain() === $this) {
            $candidature->setIdTerrain(null);
        }
    }
    return $this;
}


    
}

