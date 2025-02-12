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
    private ?Utilisateur $idUser = null;
    
    
    #[ORM\Column(length: 255)]
    #[Assert\NotBlank(message: "La localisation est obligatoire.")]
    #[Assert\Length(
        min: 3,
        max: 255,
        minMessage: "La localisation doit comporter au moins {{ limit }} caractères.",
        maxMessage: "La localisation ne peut pas dépasser {{ limit }} caractères."
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
    #[Assert\NotBlank(message: "L'image est obligatoire.")]
    #[Assert\Url(message: "L'URL de l'image n'est pas valide.")]
    private ?string $image = null;

    #[ORM\Column(length: 255)]
    private ?string $username = null;

    /**
     * @var Collection<int, Candidature>
     */
    #[ORM\OneToMany(targetEntity: Candidature::class, mappedBy: 'idTerrain')]
    private Collection $candidatures;

    public function __construct()
    {
        $this->candidatures = new ArrayCollection();
    }

    #[ORM\ManyToOne(inversedBy: 'terrains')]
    #[ORM\JoinColumn(nullable: false)]
   

    #[ORM\Column]


    public function getId(): ?int
    {
        return $this->id;
    }

    public function getLocalisation(): ?string
    {
        return $this->localisation;
    }

    public function setLocalisation(string $localisation): static
    {
        $this->localisation = $localisation;

        return $this;
    }

    public function getSuperficie(): ?float
    {
        return $this->superficie;
    }

    public function setSuperficie(float $superficie): static
    {
        $this->superficie = $superficie;

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

    public function getDescription(): ?string
    {
        return $this->description;
    }

    public function setDescription(string $description): static
    {
        $this->description = $description;

        return $this;
    }

    public function getImage(): ?string
    {
        return $this->image;
    }

    public function setImage(string $image): static
    {
        $this->image = $image;

        return $this;
    }

    public function getIdUser(): ?Utilisateur
    {
        return $this->idUser;
    }

    public function setIdUser(?Utilisateur $idUser): static
    {
        $this->idUser = $idUser;

        return $this;
    }

    public function getUsername(): ?string
    {
        return $this->username;
    }

    public function setUsername(string $username): static
    {
        $this->username = $username;

        return $this;
    }

    /**
     * @return Collection<int, Candidature>
     */
    public function getCandidatures(): Collection
    {
        return $this->candidatures;
    }

    public function addCandidature(Candidature $candidature): static
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
            // set the owning side to null (unless already changed)
            if ($candidature->getIdTerrain() === $this) {
                $candidature->setIdTerrain(null);
            }
        }

        return $this;
    }

    
}
