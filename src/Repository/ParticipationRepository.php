<?php

namespace App\Repository;

use App\Entity\Participation;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;
use App\Entity\Utilisateurs;

/**
 * @extends ServiceEntityRepository<Participation>
 */
class ParticipationRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Participation::class);
    }
    public function findByUser($user)
{
    return $this->createQueryBuilder('p')
        ->where('p.utilisateurs = :user')
        ->setParameter('user', $user)
        ->getQuery()
        ->getResult();
}
public function findByUserOrderedByFormationDate(Utilisateurs $user, string $order = 'asc')
{
    // Créer une requête DQL pour récupérer les participations d'un utilisateur triées par date de formation
    $qb = $this->createQueryBuilder('p')
        ->join('p.formation', 'f') // Joindre la table formation
        ->where('p.utilisateurs = :user') // Filtrer par l'utilisateur
        ->setParameter('user', $user);

    // Appliquer l'ordre de tri (ascendant ou descendant)
    if ($order === 'asc') {
        $qb->orderBy('f.date', 'ASC'); // Tri ascendant
    } else {
        $qb->orderBy('f.date', 'DESC'); // Tri descendant
    }

    // Exécuter la requête et retourner les résultats
    return $qb->getQuery()->getResult();
}
public function countParticipantsByFormationTitle(string $titre): int
{
    return $this->createQueryBuilder('p')
        ->join('p.formation', 'f')
        ->where('f.titre = :titre')
        ->setParameter('titre', $titre)
        ->select('COUNT(p.id)')  // Compte les participations
        ->getQuery()
        ->getSingleScalarResult();  // Renvoie un nombre entier
}


/*public function findByUserOrderedByFormationDate(Utilisateurs $user): array
{
    return $this->createQueryBuilder('p')
        ->join('p.formation', 'f') // On fait une jointure avec la table Formation
        ->where('p.utilisateurs = :user') // On filtre par utilisateur
        ->setParameter('user', $user)
        ->orderBy('f.date', 'ASC') // On trie par date de la formation (du plus proche au plus lointain)
        ->getQuery()
        ->getResult();
}



    //    /**
    //     * @return Participation[] Returns an array of Participation objects
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

    //    public function findOneBySomeField($value): ?Participation
    //    {
    //        return $this->createQueryBuilder('p')
    //            ->andWhere('p.exampleField = :val')
    //            ->setParameter('val', $value)
    //            ->getQuery()
    //            ->getOneOrNullResult()
    //        ;
    //    }
}
