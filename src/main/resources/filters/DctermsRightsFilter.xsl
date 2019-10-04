<xsl:stylesheet version="1.0" xmlns:dcterms="http://purl.org/dc/terms/" xmlns:edm="http://www.europeana.eu/schemas/edm/" xmlns:ore="http://www.openarchives.org/ore/terms/" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output encoding="utf-8" indent="yes" method="xml" omit-xml-declaration="no" standalone="yes" version="1.0" />
  <xsl:strip-space elements="*" />
  <!-- copy all -->
  <xsl:template match="@* | node()">
    <xsl:copy>
      <xsl:apply-templates select="@* | node()" />
    </xsl:copy>
  </xsl:template>
  <!-- When matching edm:WebResource/dcterms:rights do nothing -->
  <xsl:template match="edm:WebResource/dcterms:rights" />
  <!-- When matching ore:Aggregation/dcterms:rights do nothing -->
  <xsl:template match="ore:Aggregation/dcterms:rights" />
</xsl:stylesheet>
