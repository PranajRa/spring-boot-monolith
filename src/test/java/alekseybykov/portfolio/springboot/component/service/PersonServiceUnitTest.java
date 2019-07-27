package alekseybykov.portfolio.springboot.component.service;

import alekseybykov.portfolio.springboot.component.domain.Person;
import alekseybykov.portfolio.springboot.component.repository.PersonRepository;
import alekseybykov.portfolio.springboot.component.dto.PersonDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.boot.test.json.JacksonTester;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

/**
 * Clean Unit test for service: testing the Business layer in isolation from other layers
 * and without Spring context.
 *
 * Used mock() for bypassing the actual
 * dependency from the Data layer.
 *
 * Injects mock bean through constructor (lombok annotation).
 *
 * @author  aleksey.n.bykov@gmail.com
 * @version 1.0
 * @since   2019-07-14
 */
public class PersonServiceUnitTest {

    // initialized through the initFields(...) method
    private JacksonTester<PersonDTO> jacksonTester;

    private PersonRepository personRepository = mock(PersonRepository.class);

    private PersonService personService = new PersonServiceImpl(personRepository);

    @Before
    public void setup() {
        JacksonTester.initFields(this, new ObjectMapper());

        // --- behaviour for mocked dependency ---
        Optional<Person> foundedPerson = Optional.of(new Person(1L, "A", "B"));
        Mockito.when(personRepository.findById(1L))
                .thenReturn(foundedPerson);

        Person createdPerson = new Person(1L, "A", "B");
        Mockito.when(personRepository.save(createdPerson))
                .thenReturn(createdPerson);

        Person updatedPerson = new Person(1L, "C", "D");
        Mockito.when(personRepository.save(updatedPerson))
                .thenReturn(updatedPerson);

        Person deletedPerson = new Person(1L, null, null);

        doAnswer((Answer<Void>) invocation -> {
            Object[] args = invocation.getArguments();
            System.out.println("Person entity was deleted. Called with the following arguments: " + Arrays.toString(args));
            return null;
        }).when(personRepository).delete(deletedPerson);
    }

    @Test
    @SneakyThrows
    public void testCRUD() {
        // --- create person ---
        // given
        PersonDTO personDTO = PersonDTO.builder().id(1L).firstName("A").lastName("B").build();
        // when
        PersonDTO createdPersonDTO = personService.addPerson(personDTO);
        // then
        assertThat(jacksonTester.write(personDTO).getJson())
                .isEqualTo(jacksonTester.write(createdPersonDTO).getJson());

        // --- read person ---
        // when
        PersonDTO foundedPersonDTO = personService.getPersonById(1L);
        // then
        assertThat(jacksonTester.write(foundedPersonDTO).getJson())
                .isEqualTo(jacksonTester.write(createdPersonDTO).getJson());

        // --- update person ---
        // given
        PersonDTO personDtoForUpdate = PersonDTO.builder().id(1L).firstName("C").lastName("D").build();
        // when
        PersonDTO updatePersonDTO = personService.updatePerson(personDtoForUpdate);
        // then
        assertThat(jacksonTester.write(updatePersonDTO).getJson())
                .isEqualTo(jacksonTester.write(personDtoForUpdate).getJson());

        // --- delete ---
        Person personDtoForDelete = new Person(1L, null, null);
        // when
        personService.deletePerson(personDtoForDelete.getId());
        // then
        // fixme add assertion
    }
}