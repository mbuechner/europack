<xsl:stylesheet version="1.0" xmlns:ddb="http://www.deutsche-digitale-bibliothek.de/edm/" xmlns:edm="http://www.europeana.eu/schemas/edm/" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output encoding="utf-8" indent="yes" method="xml" omit-xml-declaration="no" standalone="yes" version="1.0" />
  <xsl:strip-space elements="*" />
  <!-- copy all -->
  <xsl:template match="@* | node()">
    <xsl:copy>
      <xsl:apply-templates select="@* | node()" />
    </xsl:copy>
  </xsl:template>
  <!-- When matching edm:ProvidedCHO/ddb:hierarchyPosition do nothing -->
  <xsl:template match="edm:ProvidedCHO/ddb:hierarchyType" />
</xsl:stylesheet>
