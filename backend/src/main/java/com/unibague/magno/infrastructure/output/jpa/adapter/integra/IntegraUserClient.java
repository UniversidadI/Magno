package com.unibague.magno.infrastructure.output.jpa.adapter.integra;

import com.unibague.magno.domain.exception.integra.IntegraDependencyNotFoundException;
import com.unibague.magno.domain.exception.integra.NullIntegraResponseException;
import com.unibague.magno.domain.model.enums.AcademicProgramType;
import com.unibague.magno.domain.model.integra.*;
import com.unibague.magno.domain.spi.integra.IIntegraPersistencePort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * REST client implementation of {@link IIntegraPersistencePort} for fetching data from the external Integra system.
 * <p>
 * This class is a critical component of the Magno system as it serves as the primary integration point
 * with the University of Ibagué's central information systems. It connects to:
 * <ul>
 *     <li><b>Integra API</b>: The main university system for functionaries, students, and dependencies</li>
 *     <li><b>Academia API</b>: The academic system for program information (undergraduate and postgraduate)</li>
 * </ul>
 * </p>
 * <p>
 * All HTTP communications are performed using {@link RestTemplate} with SSL certificate validation
 * disabled (configured in {@code RestTemplateConfiguration}) to handle the university's self-signed certificates.
 * </p>
 * <p>
 * <b>Important:</b> This client requires a valid API token ({@code integraApiToken}) for authentication
 * with the Integra system. Unauthorized requests will result in HTTP 401 errors.
 * </p>
 *
 * @see IIntegraPersistencePort
 * @see IntegraFunctionary
 * @see IntegraStudent
 * @see IntegraAcademicProgram
 * @see IntegraDependency
 */
public class IntegraUserClient implements IIntegraPersistencePort {

    private final RestTemplate restTemplate;
    private final String integraApiToken;
    private final String baseUrl;
    private final String allFunctionariesUrl;
    private final String studentsUrl1;
    private final String studentsUrl2;
    private final String allStudentsUrl;
    private final String academicProgramsUrl;
    private final String dependenciesUrl;
    private final String academiaAcademicProgramsGeneralUrl;

    /** Mode identifier for undergraduate programs in the Academia API */
    private static final String UNDERGRADUATE = "1";
    /** Mode identifier for postgraduate programs in the Academia API */
    private static final String POSTGRADUATE = "2";

    /**
     * Constructs a new IntegraUserClient with all required configuration parameters.
     * <p>
     * This constructor is called by {@code BeanConfiguration} which injects the values
     * from environment variables defined in {@code application.properties}.
     * </p>
     *
     * @param restTemplate                      the configured RestTemplate for HTTP communications
     * @param integraApiToken                   the API authentication token for Integra system
     * @param baseUrl                           the base URL of the Integra API (e.g., http://integra.unibague.edu.co/)
     * @param allFunctionariesUrl               the endpoint path for retrieving all functionaries
     * @param studentsUrl1                      the first part of the student info endpoint (before identification)
     * @param studentsUrl2                      the second part of the student info endpoint (after identification)
     * @param allStudentsUrl                    the endpoint path for retrieving all students
     * @param academicProgramsUrl               the endpoint path for retrieving academic programs from Integra
     * @param dependenciesUrl                   the endpoint path for retrieving organizational dependencies
     * @param academiaAcademicProgramsGeneralUrl the base URL for Academia academic programs API
     */
    public IntegraUserClient(
            RestTemplate restTemplate,
            String integraApiToken,
            String baseUrl,
            String allFunctionariesUrl,
            String studentsUrl1,
            String studentsUrl2,
            String allStudentsUrl,
            String academicProgramsUrl,
            String dependenciesUrl,
            String academiaAcademicProgramsGeneralUrl
    ) {
        this.restTemplate = restTemplate;
        this.integraApiToken = integraApiToken;
        this.baseUrl = baseUrl;
        this.allFunctionariesUrl = allFunctionariesUrl;
        this.studentsUrl1 = studentsUrl1;
        this.studentsUrl2 = studentsUrl2;
        this.allStudentsUrl = allStudentsUrl;
        this.academicProgramsUrl = academicProgramsUrl;
        this.dependenciesUrl = dependenciesUrl;
        this.academiaAcademicProgramsGeneralUrl = academiaAcademicProgramsGeneralUrl;
    }

    /**
     * Retrieves all undergraduate academic programs from the Academia system.
     * <p>
     * This method calls the Academia API with mode "1" (undergraduate) to fetch
     * all active undergraduate programs offered by the university.
     * </p>
     *
     * @return a list of {@link AcademiaAcademicProgram} representing all undergraduate programs
     * @throws NullIntegraResponseException if the Academia API returns a null response body
     * @throws org.springframework.web.client.RestClientException if there's a communication error with the API
     */
    public List<AcademiaAcademicProgram> getAcademiaAcademicPrograms1(){
        String path = academiaAcademicProgramsGeneralUrl + UNDERGRADUATE;
        ResponseEntity<List<AcademiaAcademicProgram>> response = restTemplate.exchange(
                path,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<AcademiaAcademicProgram>>() {}
        );

        if (response.getBody() == null) {
            throw new NullIntegraResponseException();
        }

        return response.getBody();
    }

    /**
     * Retrieves all postgraduate academic programs from the Academia system.
     * <p>
     * This method calls the Academia API with mode "2" (postgraduate) to fetch
     * all active postgraduate programs (specializations, master's, doctorates) offered by the university.
     * </p>
     *
     * @return a list of {@link AcademiaAcademicProgram} representing all postgraduate programs
     * @throws NullIntegraResponseException if the Academia API returns a null response body
     * @throws org.springframework.web.client.RestClientException if there's a communication error with the API
     */
    public List<AcademiaAcademicProgram> getAcademiaAcademicPrograms2(){
        String path = academiaAcademicProgramsGeneralUrl + POSTGRADUATE;
        ResponseEntity<List<AcademiaAcademicProgram>> response = restTemplate.exchange(
                path,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<AcademiaAcademicProgram>>() {}
        );

        if (response.getBody() == null) {
            throw new NullIntegraResponseException();
        }

        return response.getBody();
    }

    /**
     * Retrieves all functionaries (employees) registered in the Integra system.
     * <p>
     * This method fetches the complete list of university employees including professors,
     * administrative staff, and other personnel. The data includes identification,
     * name, email, dependency, and other employment-related information.
     * </p>
     * <p>
     * <b>Note:</b> This operation may return a large dataset and is typically used
     * during system initialization or synchronization processes.
     * </p>
     *
     * @return a list of {@link IntegraFunctionary} representing all university employees
     * @throws NullIntegraResponseException if the Integra API returns a null response body
     * @throws org.springframework.web.client.HttpClientErrorException.Unauthorized if the API token is invalid
     * @throws org.springframework.web.client.RestClientException if there's a communication error with the API
     */
    @Override
    public List<IntegraFunctionary> getAllFunctionaries() {
        final String url = baseUrl + allFunctionariesUrl + integraApiToken;

        ResponseEntity<List<IntegraFunctionary>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<IntegraFunctionary>>() {}
        );
        if (response.getBody() == null) {
            throw new NullIntegraResponseException();
        }
        return response.getBody();
    }

    /**
     * Retrieves all academic records for a specific student identified by their identification number.
     * <p>
     * A student may have multiple records if they are enrolled in more than one academic program
     * (e.g., double degree, or sequential programs). Each record contains information about
     * a specific enrollment including the program, semester, and status.
     * </p>
     *
     * @param identification the student's identification number (cédula)
     * @return a list of {@link IntegraStudent} records for the specified student
     * @throws NullIntegraResponseException if the Integra API returns a null response body
     * @throws org.springframework.web.client.HttpClientErrorException.Unauthorized if the API token is invalid
     * @throws org.springframework.web.client.RestClientException if there's a communication error with the API
     */
    @Override
    public List<IntegraStudent> getIntegraStudentRecordsByIdentification(String identification) {
        final String url = baseUrl + studentsUrl1 + integraApiToken + "&code_user=" + identification + studentsUrl2;

        ResponseEntity<List<IntegraStudent>> response = getListResponseEntity(url);
        if (response.getBody() == null) {
            throw new NullIntegraResponseException();
        }
        return response.getBody();
    }

    /**
     * Retrieves all academic programs from both Integra and Academia systems, merged and deduplicated.
     * <p>
     * This method combines data from three sources:
     * <ul>
     *     <li>Integra's deprecated academic programs endpoint</li>
     *     <li>Academia's undergraduate programs (mode 1)</li>
     *     <li>Academia's postgraduate programs (mode 2)</li>
     * </ul>
     * Programs are deduplicated by program code, keeping the first occurrence if duplicates exist.
     * </p>
     *
     * @return a deduplicated list of {@link IntegraAcademicProgram} from all sources
     * @throws NullIntegraResponseException if any of the API calls returns a null response body
     * @throws org.springframework.web.client.RestClientException if there's a communication error with any API
     */
    public List<IntegraAcademicProgram> getAllAcademicPrograms() {

        List<IntegraAcademicProgram> integraPrograms = getAllAcademicProgramsDeprecated();
        List<AcademiaAcademicProgram> academiaUndergrad = getAcademiaAcademicPrograms1();
        List<AcademiaAcademicProgram> academiaPostgrad = getAcademiaAcademicPrograms2();

        List<IntegraAcademicProgram> academiaPrograms = Stream.concat(
                        academiaUndergrad.stream(),
                        academiaPostgrad.stream()
                ).map(this::mapAcademiaToIntegra)
                .toList();

        List<IntegraAcademicProgram> allPrograms = new ArrayList<>();
        allPrograms.addAll(integraPrograms);
        allPrograms.addAll(academiaPrograms);

        return allPrograms.stream()
                .collect(Collectors.toMap(
                        IntegraAcademicProgram::getProgramCode,
                        program -> program,
                        (existing, replacement) -> existing
                ))
                .values()
                .stream()
                .toList();
    }

    /**
     * Maps an {@link AcademiaAcademicProgram} to an {@link IntegraAcademicProgram}.
     * <p>
     * This conversion is necessary because the Academia and Integra systems use different
     * data structures for academic programs. Some fields (like facultyId and isActive)
     * are set to null as they are not available in the Academia source.
     * </p>
     *
     * @param academia the Academia academic program to convert
     * @return an equivalent {@link IntegraAcademicProgram} object
     */
    private IntegraAcademicProgram mapAcademiaToIntegra(AcademiaAcademicProgram academia) {
        return new IntegraAcademicProgram(
                academia.getProgramCode(),
                academia.getProgramName(),
                null,
                academia.getSedeName(),
                academia.getMethodology(),
                academia.getModality(),
                null
        );
    }

    /**
     * Retrieves academic programs from the legacy Integra API endpoint.
     * <p>
     * <b>Deprecated source:</b> This method accesses an older Integra endpoint that may have
     * incomplete or outdated program information. It is combined with Academia data
     * in {@link #getAllAcademicPrograms()} for comprehensive coverage.
     * </p>
     *
     * @return a list of {@link IntegraAcademicProgram} from the legacy Integra endpoint
     * @throws NullIntegraResponseException if the Integra API returns a null response body
     * @throws org.springframework.web.client.HttpClientErrorException.Unauthorized if the API token is invalid
     */
    private List<IntegraAcademicProgram> getAllAcademicProgramsDeprecated() {
        final String url = baseUrl + academicProgramsUrl + integraApiToken;

        ResponseEntity<List<IntegraAcademicProgram>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<IntegraAcademicProgram>>() {}
        );

        if (response.getBody() == null) {
            throw new NullIntegraResponseException();
        }
        return response.getBody();
    }

    /**
     * Retrieves all organizational dependencies (departments, faculties, units) from Integra.
     * <p>
     * Dependencies represent the organizational structure of the university, including
     * faculties, departments, research centers, and administrative units. Each functionary
     * is associated with one or more dependencies.
     * </p>
     *
     * @return a list of {@link IntegraDependency} representing all organizational units
     * @throws NullIntegraResponseException if the Integra API returns a null response body
     * @throws org.springframework.web.client.HttpClientErrorException.Unauthorized if the API token is invalid
     */
    private List<IntegraDependency> getAllDependencies() {
        final String url = baseUrl + dependenciesUrl + integraApiToken;

        ResponseEntity<List<IntegraDependency>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<IntegraDependency>>() {}
        );
        if (response.getBody() == null) {
            throw new NullIntegraResponseException();
        }
        return response.getBody();
    }

    /**
     * Retrieves all students currently registered in the Integra system.
     * <p>
     * This method returns all active student records from the university. Each student
     * may have multiple records if enrolled in different programs.
     * </p>
     * <p>
     * <b>Note:</b> This operation returns a large dataset and should be used carefully,
     * typically during system synchronization processes.
     * </p>
     *
     * @return a list of {@link IntegraStudent} representing all registered students
     * @throws NullIntegraResponseException if the Integra API returns a null response body
     * @throws org.springframework.web.client.HttpClientErrorException.Unauthorized if the API token is invalid
     */
    public List<IntegraStudent> getAllStudents() {
        final String url = baseUrl + allStudentsUrl + integraApiToken;

        ResponseEntity<List<IntegraStudent>> response = getListResponseEntity(url);
        if (response.getBody() == null) {
            throw new NullIntegraResponseException();
        }
        return response.getBody();
    }

    /**
     * Retrieves all academic programs from both Integra and Academia sources, organized by program type.
     * <p>
     * This method consolidates academic program data from multiple sources and categorizes them:
     * <ul>
     *     <li><b>PREGRADO (Undergraduate):</b> Programs from Integra + Academia undergraduate endpoint</li>
     *     <li><b>POSGRADO (Postgraduate):</b> Programs from Academia postgraduate endpoint</li>
     * </ul>
     * </p>
     * <p>
     * Deduplication is performed to ensure each program code appears only once:
     * <ul>
     *     <li>Within undergraduate: first occurrence is kept</li>
     *     <li>For postgraduate: programs already in undergraduate are excluded</li>
     * </ul>
     * </p>
     *
     * @return a map where keys are {@link AcademicProgramType} (PREGRADO or POSGRADO)
     *         and values are lists of unique {@link IntegraAcademicProgram} objects
     * @throws NullIntegraResponseException if any of the API calls returns a null response body
     * @throws org.springframework.web.client.RestClientException if there's a communication error with any API
     */
    @Override
    public Map<AcademicProgramType, List<IntegraAcademicProgram>> getAllAcademicProgramsMappedByType() {
        List<IntegraAcademicProgram> integraPrograms = getAllAcademicProgramsDeprecated();
        List<AcademiaAcademicProgram> academiaUndergrad = getAcademiaAcademicPrograms1();
        List<AcademiaAcademicProgram> academiaPostgrad = getAcademiaAcademicPrograms2();


        // Undergraduate programs
        List<IntegraAcademicProgram> undergradPrograms = new ArrayList<>();
        undergradPrograms.addAll(integraPrograms);
        undergradPrograms.addAll(academiaUndergrad.stream()
                .map(this::mapAcademiaToIntegra)
                .toList());

        // Postgraduate programs
        List<IntegraAcademicProgram> postgradPrograms = academiaPostgrad.stream()
                .map(this::mapAcademiaToIntegra)
                .toList();

        // Delete duplicates within each type, keeping the first occurrence
        Map<String, IntegraAcademicProgram> uniqueUndergrad = undergradPrograms.stream()
                .collect(Collectors.toMap(
                        IntegraAcademicProgram::getProgramCode,
                        program -> program,
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));

        Map<String, IntegraAcademicProgram> uniquePostgrad = postgradPrograms.stream()
                .filter(p -> !uniqueUndergrad.containsKey(p.getProgramCode())) // Avoid duplicates across types
                .collect(Collectors.toMap(
                        IntegraAcademicProgram::getProgramCode,
                        program -> program,
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));

        Map<AcademicProgramType, List<IntegraAcademicProgram>> result = new EnumMap<>(AcademicProgramType.class);
        result.put(AcademicProgramType.PREGRADO, new ArrayList<>(uniqueUndergrad.values()));
        result.put(AcademicProgramType.POSGRADO, new ArrayList<>(uniquePostgrad.values()));

        return result;
    }

    /**
     * Retrieves academic programs that match a specific set of program codes.
     * <p>
     * This method filters the complete list of academic programs (from all sources)
     * to return only those whose program codes are in the provided set.
     * Useful for fetching specific programs when their codes are already known.
     * </p>
     *
     * @param programCodes a set of program codes to search for
     * @return a list of {@link IntegraAcademicProgram} matching the specified codes
     * @throws NullIntegraResponseException if any underlying API call returns a null response
     * @see #getAllAcademicPrograms()
     */
    @Override
    public List<IntegraAcademicProgram> getIntegraAcademicProgramsByProgramCodes(Set<String> programCodes) {
        return getAllAcademicPrograms().stream()
                .filter(academicProgram -> programCodes.contains(academicProgram.getProgramCode()))
                .toList();
    }

    /**
     * Finds an organizational dependency by its exact name.
     * <p>
     * Searches through all university dependencies (faculties, departments, units)
     * to find one with an exactly matching name. This is used to link functionaries
     * to their organizational units.
     * </p>
     *
     * @param dependencyName the exact name of the dependency to find
     * @return the {@link IntegraDependency} with the matching name
     * @throws IntegraDependencyNotFoundException if no dependency with the specified name exists
     * @throws NullIntegraResponseException if the Integra API returns a null response body
     * @throws org.springframework.web.client.HttpClientErrorException.Unauthorized if the API token is invalid
     */
    @Override
    public IntegraDependency getIntegraDependencyByDependencyName(String dependencyName) {
        return getAllDependencies().stream()
                .filter(dependency -> dependency.getDepName().equals(dependencyName))
                .findFirst()
                .orElseThrow(() -> {
                    String message = String.format("Integra dependency with name %s not found", dependencyName);
                    return new IntegraDependencyNotFoundException(message);
                });
    }

    /**
     * Finds a functionary (university employee) by their email address.
     * <p>
     * Searches the complete list of functionaries to find one with an exactly matching
     * email address. This is typically used during user authentication to verify if
     * a user is a registered employee of the university.
     * </p>
     *
     * @param email the email address to search for (must be exact match)
     * @return an {@link Optional} containing the {@link IntegraFunctionary} if found,
     *         or {@link Optional#empty()} if no functionary has that email
     * @throws NullIntegraResponseException if the Integra API returns a null response body
     * @throws org.springframework.web.client.HttpClientErrorException.Unauthorized if the API token is invalid
     */
    @Override
    public Optional<IntegraFunctionary> getIntegraFunctionaryByEmail(String email) {
        return getAllFunctionaries()
                .stream()
                .filter(functionary -> functionary.getEmail().equals(email))
                .findFirst();
    }

    /**
     * Finds a student by their email address.
     * <p>
     * Searches the complete list of students to find one with an exactly matching
     * email address. This is typically used during user authentication to verify if
     * a user is a registered student of the university.
     * </p>
     * <p>
     * <b>Note:</b> If a student is enrolled in multiple programs, only the first
     * matching record is returned. Use {@link #getIntegraStudentRecordsByIdentification(String)}
     * to retrieve all enrollment records for a student.
     * </p>
     *
     * @param email the email address to search for (must be exact match)
     * @return an {@link Optional} containing the {@link IntegraStudent} if found,
     *         or {@link Optional#empty()} if no student has that email
     * @throws NullIntegraResponseException if the Integra API returns a null response body
     * @throws org.springframework.web.client.HttpClientErrorException.Unauthorized if the API token is invalid
     */
    @Override
    public Optional<IntegraStudent> getIntegraStudentByEmail(String email) {
        return getAllStudents()
                .stream()
                .filter(student -> student.getEmail().equals(email))
                .findFirst();
    }

    /**
     * Executes a GET request to fetch a list of students from the specified URL.
     * <p>
     * This is a utility method used internally to avoid code duplication when
     * making similar REST calls that return student lists.
     * </p>
     *
     * @param url the complete URL to call (including base URL and parameters)
     * @return a {@link ResponseEntity} containing the list of {@link IntegraStudent} objects
     */
    private ResponseEntity<List<IntegraStudent>> getListResponseEntity(String url) {
        return restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<IntegraStudent>>() {}
        );
    }

}