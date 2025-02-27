<?php

namespace App\Service;

use Nucleos\DompdfBundle\Factory\DompdfFactoryInterface;
use Nucleos\DompdfBundle\Wrapper\DompdfWrapperInterface;
use Symfony\Component\HttpFoundation\StreamedResponse;

class PdfGeneratorService
{
    public function __construct(
        private readonly DompdfFactoryInterface $factory,
        private readonly DompdfWrapperInterface $wrapper
    ){}

    public function getpdf(string $html)
    {
        return $this->wrapper->getpdf($html);
    }   
    
    public function getStreamResponse(string $html, string $filename): StreamedResponse
    {
        return $this->wrapper->getStreamResponse($html, $filename);
    }

    public function output(string $html): string
    {
        $dompdf = $this->factory->create();
        $dompdf->loadHtml($html);
        $dompdf->setPaper('A4');
        $dompdf->render();

        return $dompdf->output();
    }   
}

