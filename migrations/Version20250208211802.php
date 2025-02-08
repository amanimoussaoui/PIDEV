<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: Please modify to your needs!
 */
final class Version20250208211802 extends AbstractMigration
{
    public function getDescription(): string
    {
        return '';
    }

    public function up(Schema $schema): void
    {
        // this up() migration is auto-generated, please modify it to your needs
        $this->addSql('CREATE TABLE culture (id INT AUTO_INCREMENT NOT NULL, parcelle_id INT NOT NULL, nom_culture VARCHAR(100) NOT NULL, date_semis DATE NOT NULL, duree INT NOT NULL, statut VARCHAR(50) NOT NULL, INDEX IDX_B6A99CEB4433ED66 (parcelle_id), PRIMARY KEY(id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB');
        $this->addSql('ALTER TABLE culture ADD CONSTRAINT FK_B6A99CEB4433ED66 FOREIGN KEY (parcelle_id) REFERENCES parcelle (id)');
    }

    public function down(Schema $schema): void
    {
        // this down() migration is auto-generated, please modify it to your needs
        $this->addSql('ALTER TABLE culture DROP FOREIGN KEY FK_B6A99CEB4433ED66');
        $this->addSql('DROP TABLE culture');
    }
}
