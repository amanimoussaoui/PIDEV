<?php

namespace App\Entity;

use App\Repository\ProductRepository;
use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Validator\Constraints as Assert;


#[ORM\Entity(repositoryClass: ProductRepository::class)]
class Product
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\Column(length: 255)]
    #[Assert\NotBlank(message: "Le nom ne peut pas être vide.")]
    #[Assert\Type(type: "string", message: "Le nom doit être une chaîne de caractères.")]
    private ?string $nom = null;

    #[ORM\Column(length: 255)]
    #[Assert\NotBlank(message: "La description ne peut pas être vide.")]
    #[Assert\Type(type: "string", message: "La description doit être une chaîne de caractères.")]
    private ?string $description = null;

    #[ORM\Column]
#[Assert\NotBlank(message: "Le prix ne peut pas être vide.")]
#[Assert\Type(type: "numeric", message: "Le prix doit être un nombre.")]
#[Assert\PositiveOrZero(message: "Le prix doit être un nombre positif ou zéro.")]
#[Assert\Regex(
    pattern: "/^\d+(\.\d{1,2})?$/",
    message: "Le prix doit être un nombre valide sans lettres."
)]
private ?float $prix = null;


    #[ORM\Column]
    #[Assert\NotBlank(message: "Le stock ne peut pas être vide.")]
    #[Assert\Type(type: "integer", message: "Le stock doit être un nombre entier.")]
    #[Assert\PositiveOrZero(message: "Le stock doit être un nombre positif ou zéro.")]
    private ?int $stock = null;

    #[ORM\Column(length: 255)]
    #[Assert\NotBlank(message: "La catégorie ne peut pas être vide.")]
    private ?string $category = null;
    

    #[ORM\Column(length: 255)]
    #[Assert\NotBlank(message: "L'image ne peut pas être vide.")]
    #[Assert\Type(type: "string", message: "L'image doit être une URL sous forme de texte.")]
    private ?string $image = null;

    #[ORM\ManyToOne(targetEntity: User::class, inversedBy: 'products')]
    #[ORM\JoinColumn(nullable: false)]
    #[Assert\NotBlank(message: "L'utilisateur associé ne peut pas être vide.")]
    private ?User $user = null;

    public function getId(): ?int { return $this->id; }

    public function getNom(): ?string { return $this->nom; }
    public function setNom(string $nom): static { $this->nom = $nom; return $this; }

    public function getDescription(): ?string { return $this->description; }
    public function setDescription(string $description): static { $this->description = $description; return $this; }

    public function getPrix(): ?float { return $this->prix; }
    public function setPrix(float $prix): static { $this->prix = $prix; return $this; }

    public function getStock(): ?int { return $this->stock; }
    public function setStock(int $stock): static { $this->stock = $stock; return $this; }

    public function getCategory(): ?string { return $this->category; }
    public function setCategory(string $category): static { $this->category = $category; return $this; }

    public function getImage(): ?string { return $this->image; }
    public function setImage(string $image): static { $this->image = $image; return $this; }

    public function getUser(): ?User { return $this->user; }
    public function setUser(?User $user): static { $this->user = $user; return $this; }
}
