<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: Please modify to your needs!
 */
final class Version20250209172802 extends AbstractMigration
{
    public function getDescription(): string
    {
        return '';
    }

    public function up(Schema $schema): void
    {
        // this up() migration is auto-generated, please modify it to your needs
        $this->addSql('CREATE TABLE activite (id INT AUTO_INCREMENT NOT NULL, parcelle_id INT DEFAULT NULL, culture_id INT DEFAULT NULL, description LONGTEXT NOT NULL, type VARCHAR(50) NOT NULL, date DATE NOT NULL, INDEX IDX_B87555154433ED66 (parcelle_id), INDEX IDX_B8755515B108249D (culture_id), PRIMARY KEY(id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB');
        $this->addSql('ALTER TABLE activite ADD CONSTRAINT FK_B87555154433ED66 FOREIGN KEY (parcelle_id) REFERENCES parcelle (id)');
        $this->addSql('ALTER TABLE activite ADD CONSTRAINT FK_B8755515B108249D FOREIGN KEY (culture_id) REFERENCES culture (id)');
    }

    public function down(Schema $schema): void
    {
        // this down() migration is auto-generated, please modify it to your needs
        $this->addSql('ALTER TABLE activite DROP FOREIGN KEY FK_B87555154433ED66');
        $this->addSql('ALTER TABLE activite DROP FOREIGN KEY FK_B8755515B108249D');
        $this->addSql('DROP TABLE activite');
    }
}
