![](https://github.com/mbuechner/europack/actions/workflows/maven.yml/badge.svg)
# Europack
Europack is a functional interface which allows anyone to select datasets from [Deutsche Digitale Bibliothek](https://www.deutsche-digitale-bibliothek.de/) according to set criteria and generate a valid external EDM data export.

## Purpose
It should be possible to easily generate the DDB’s exports by selecting the datasets according to certain criteria. The selection of the datasets should be possible in both the preview system and in the production system.  The outline of the interface will be based on the requirements identified during the process of delivering DDB data to Europeana in the last years, when the work was carried out more or less manually. 

## Needs and Benefits
The benefits of using an interface as described above over exporting the datasets using Europack is the possibility of selecting the datasets according to certain desired criteria and filtering out DDB-specific features (such as hierarchies). Delivering data over OAI-PMH didn’t offer these features and the harvesting couldn’t be carried out so that only the desired records would be fetched.

## Scope
1. Selection
    - Select the environment where the dataset is
        - Production system
        - Test system
    - Select the datasets/objects that need to be sent to Europeana by
        - Provider
        - Mediatype
        - Data source format
        - Rights (for metadata and for the media content)
        - Select the Dataset by ID or label
    - Process the objects by filtering out DDB-specific features 
    - Validate against external EDM schema
2. Filtering out the DDB-specific features
    - Hierarchical records
    - Elements containing URIs (`edm:Provider` and `edm:DataProvider` that contain the DDB URIs. These elements should be cardinality 1 and ideally contain a DND-URI)
3. Proof EDM external using/implementing Europeana’s Metis External Validation Service schema
    - (TBD)
4. Generating a valid external EDM file (and possibly archive it into a ZIP)
5. Saving the ZIP file at a determined location

## Downloads
See https://github.com/mbuechner/europack/releases/

## Screenshots
<p align="center">
 <a target="_blank" rel="noopener noreferrer" href="https://github.com/mbuechner/europack/blob/master/europack3.1-01.png">
  <img src="https://github.com/mbuechner/europack/raw/master/europack3.1-01.png" width="256" alt="Window #1" title="Screenshot of first Window">
 </a>
 <a target="_blank" rel="noopener noreferrer" href="https://github.com/mbuechner/europack/blob/master/europack3.1-02.png">
  <img src="https://github.com/mbuechner/europack/raw/master/europack3.1-02.png" width="256" alt="Window #2" title="Screenshot of second Window">
 </a>
 <a target="_blank" rel="noopener noreferrer" href="https://github.com/mbuechner/europack/blob/master/europack3.1-03.png">
  <img src="https://github.com/mbuechner/europack/raw/master/europack3.1-03.png" width="256" alt="Window #3" title="Screenshot of third Window">
 </a>
 <a target="_blank" rel="noopener noreferrer" href="https://github.com/mbuechner/europack/blob/master/europack3.1-04.png">
  <img src="https://github.com/mbuechner/europack/raw/master/europack3.1-04.png" width="256" alt="Window #4" title="Screenshot of fourth Window">
 </a>
 <a target="_blank" rel="noopener noreferrer" href="https://github.com/mbuechner/europack/blob/master/europack3.1-05.png">
  <img src="https://github.com/mbuechner/europack/raw/master/europack3.1-05.png" width="256" alt="Window #5" title="Screenshot of fifth Window">
 </a>
</p>

## Compile & Deploy
1. Download Maven Project from this repository.
2. Install [Maven](https://maven.apache.org/) project management tool.
3. Run in the folder with `pom.xml` the following command: `mvn clean package`
4. The archive `target\europack.zip` does contain everything you'll need to run the application.

*Supported Operating Systems:* Windows, Linux and MacOS

*Requirement:* Java(TM) SE Runtime Environment (11 or newer) _or_ OpenJDK (11 or newer)
