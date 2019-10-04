<xsl:stylesheet version="1.0" xmlns:dcterms="http://purl.org/dc/terms/" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output encoding="utf-8" indent="yes" method="xml" omit-xml-declaration="no" standalone="yes" version="1.0" />
  <xsl:strip-space elements="*" />
  <!-- copy all -->
  <xsl:template match="@* | node()">
    <xsl:copy>
      <xsl:apply-templates select="@* | node()" />
    </xsl:copy>
  </xsl:template>
  <!-- When matching edm:ProvidedCHO/dcterms:language do nothing -->
  <xsl:template match="dcterms:LinguisticSystem" />
</xsl:stylesheet>
