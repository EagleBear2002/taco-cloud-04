package tacos;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import tacos.Ingredient.Type;
import tacos.web.DesignTacoController;

@ExtendWith(SpringExtension.class) // <1>
@WebMvcTest(DesignTacoController.class)
public class DesignTacoControllerTest {
	
	@Autowired
	private MockMvc mockMvc;
	
	private List<Ingredient> ingredients;
	
	@BeforeEach
	public void setup() {
		ingredients = Arrays.asList(
				new Ingredient("FLTO", "Flour Tortilla", Type.WRAP),
				new Ingredient("COTO", "Corn Tortilla", Type.WRAP),
				new Ingredient("GRBF", "Ground Beef", Type.PROTEIN),
				new Ingredient("CARN", "Carnitas", Type.PROTEIN),
				new Ingredient("TMTO", "Diced Tomatoes", Type.VEGGIES),
				new Ingredient("LETC", "Lettuce", Type.VEGGIES),
				new Ingredient("CHED", "Cheddar", Type.CHEESE),
				new Ingredient("JACK", "Monterrey Jack", Type.CHEESE),
				new Ingredient("SLSA", "Salsa", Type.SAUCE),
				new Ingredient("SRCR", "Sour Cream", Type.SAUCE)
		);
		
	}
	
	@Test
	public void testShowDesignForm() throws Exception {
		mockMvc.perform(get("/design"))
				.andExpect(status().isOk())
				.andExpect(view().name("design"))
				.andExpect(model().attribute("wrap", ingredients.subList(0, 2)))
				.andExpect(model().attribute("protein", ingredients.subList(2, 4)))
				.andExpect(model().attribute("veggies", ingredients.subList(4, 6)))
				.andExpect(model().attribute("cheese", ingredients.subList(6, 8)))
				.andExpect(model().attribute("sauce", ingredients.subList(8, 10)));
	}
	
	@Test
	public void processTaco() throws Exception {
		mockMvc.perform(post("/design")
						.content("name=Test+Taco&ingredients=FLTO,GRBF,CHED")
						.contentType(MediaType.APPLICATION_FORM_URLENCODED))
				.andExpect(status().is3xxRedirection())
				.andExpect(header().stringValues("Location", "/orders/current"));
	}
	
}
