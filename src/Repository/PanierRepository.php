<?php

namespace App\Repository;

use App\Entity\Panier;
use App\Entity\Utilisateurs;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * @extends ServiceEntityRepository<Panier>
 */
class PanierRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Panier::class);
    }

    //    /**
    //     * @return Panier[] Returns an array of Panier objects
    //     */
    //    public function findByExampleField($value): array
    //    {
    //        return $this->createQueryBuilder('p')
    //            ->andWhere('p.exampleField = :val')
    //            ->setParameter('val', $value)
    //            ->orderBy('p.id', 'ASC')
    //            ->setMaxResults(10)
    //            ->getQuery()
    //            ->getResult()
    //        ;
    //    }

    //    public function findOneBySomeField($value): ?Panier
    //    {
    //        return $this->createQueryBuilder('p')
    //            ->andWhere('p.exampleField = :val')
    //            ->setParameter('val', $value)
    //            ->getQuery()
    //            ->getOneOrNullResult()
    //        ;
    //    }
  /*  public function findByUser($user)
{
    return $this->createQueryBuilder('p')
        ->join('p.commande', 'c')
        ->andWhere('c.utilisateurs = :user')
        ->setParameter('user', $user)
        ->getQuery()
        ->getResult();
}*/
 /**
     * Trouver les paniers associés à un utilisateur via ses commandes.
     *
     * @param Utilisateurs $user
     * @return Panier[] Returns an array of Panier objects
     */
    public function findByUser(Utilisateurs $user): array
    {
        return $this->createQueryBuilder('p')
            ->innerJoin('p.commande', 'c') // Assurez-vous que la relation entre Panier et Commande est correcte
            ->where('c.utilisateurs = :user') // Filtrer les paniers par utilisateur via la commande
            ->setParameter('user', $user)
            ->getQuery()
            ->getResult();

}}
