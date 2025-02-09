<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: Please modify to your needs!
 */
final class Version20250209154911 extends AbstractMigration
{
    public function getDescription(): string
    {
        return '';
    }

    public function up(Schema $schema): void
    {
        // this up() migration is auto-generated, please modify it to your needs
        $this->addSql('CREATE TABLE recolte (id INT AUTO_INCREMENT NOT NULL, culture_id INT NOT NULL, date_recolte DATE NOT NULL, quantite DOUBLE PRECISION NOT NULL, qualite VARCHAR(50) NOT NULL, prix_unitaire DOUBLE PRECISION NOT NULL, INDEX IDX_3433713CB108249D (culture_id), PRIMARY KEY(id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB');
        $this->addSql('ALTER TABLE recolte ADD CONSTRAINT FK_3433713CB108249D FOREIGN KEY (culture_id) REFERENCES culture (id)');
        $this->addSql('ALTER TABLE culture CHANGE date_semis date_semis DATE DEFAULT NULL');
    }

    public function down(Schema $schema): void
    {
        // this down() migration is auto-generated, please modify it to your needs
        $this->addSql('ALTER TABLE recolte DROP FOREIGN KEY FK_3433713CB108249D');
        $this->addSql('DROP TABLE recolte');
        $this->addSql('ALTER TABLE culture CHANGE date_semis date_semis DATE NOT NULL');
    }
}
