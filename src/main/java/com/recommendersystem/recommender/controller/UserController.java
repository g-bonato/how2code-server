package com.recommendersystem.recommender.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.recommendersystem.recommender.models.User;
import com.recommendersystem.recommender.repository.UserRepository;
import com.recommendersystem.recommender.utils.StringUtil;

@CrossOrigin
@RestController
@RequestMapping("/user")
public class UserController {
	@Autowired
	private UserRepository repository;

	@RequestMapping(value = "/", method = RequestMethod.POST)
	public Map<String, Object> createUser(@Valid @RequestBody User user, HttpServletResponse httpResponse) {
		Map<String, Object> response = new HashMap<>();

		if (StringUtil.isBlank(user.getEmail()) || StringUtil.isBlank(user.getPassword())) {
			response.put("success", false);
			response.put("message", "Email e senha são obrigatórios");

			return response;
		}

		if (!repository.findByEmail(user.getEmail()).isEmpty()) {
			response.put("success", false);
			response.put("message", "Email já está cadastrado");

			return response;
		}

		user.set_id(ObjectId.get());

		user.setSession(SessionController.createSession());
		repository.save(user);

		httpResponse.addCookie(getCookie("session", user.getSession().getSessionId()));
		httpResponse.addCookie(getCookie("userId", user.getId()));

		response.put("user", user);
		response.put("success", true);

		return response;
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	public Map<String, Object> deleteUser(@PathVariable String id) {
		Optional<User> userAux = repository.findById(id);

		Map<String, Object> response = new HashMap<>();

		if (!userAux.isPresent()) {
			response.put("message", "Usuário não encontrado!");
			response.put("success", false);

			return response;
		}

		repository.delete(userAux.get());

		response.put("success", true);

		return response;
	}

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public Map<String, Object> getAllUsers() {
		Map<String, Object> response = new HashMap<>();

		response.put("users", repository.findAll());
		response.put("success", true);

		return response;
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public Map<String, Object> getUserById(@PathVariable("id") String id) {
		Optional<User> userAux = repository.findById(id);

		Map<String, Object> response = new HashMap<>();

		if (!userAux.isPresent()) {
			response.put("message", "Usuário não encontrado!");
			response.put("success", false);

			return response;
		}

		response.put("user", userAux.get());
		response.put("success", true);

		return response;
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.PUT)
	public Map<String, Object> modifyUserById(@PathVariable("id") String id, @Valid @RequestBody User user) {
		Optional<User> userAux = repository.findById(id);

		Map<String, Object> response = new HashMap<>();

		if (!userAux.isPresent()) {
			response.put("message", "Usuário não encontrado!");
			response.put("success", false);

			return response;
		}

		User userOld = userAux.get();

		userOld.setEmail(user.getEmail());
		userOld.setFullname(user.getFullname());
		userOld.setIfsulStudent(user.getIfsulStudent());
		userOld.setImage(user.getImage());

		if (StringUtil.isNotBlank(user.getPassword())) {
			userOld.setPassword(user.getPassword());
		} else {
			userOld.setPassword(userOld.getPassword());
		}

		repository.save(userOld);

		response.put("user", userOld);
		response.put("success", true);

		return response;
	}

	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public Map<String, Object> userLogin(@RequestBody User userForLogin, HttpServletResponse httpResponse) {
		Map<String, Object> response = new HashMap<>();

		List<User> users = repository.findByEmailAndPassword(userForLogin.getEmail(), userForLogin.getPassword());

		if (users.isEmpty()) {
			response.put("success", false);
			response.put("message", "Email ou senha incorretos!");

			return response;
		}

		User user = users.get(0);
		user.setSession(SessionController.createSession());
		repository.save(user);

		response.put("user", user);
		response.put("success", true);

		httpResponse.addCookie(getCookie("session", user.getSession().getSessionId()));
		httpResponse.addCookie(getCookie("userId", user.getId()));

		return response;
	}

	@RequestMapping(value = "/logout", method = RequestMethod.POST)
	public Map<String, Object> userLogout(@RequestBody User userForLogout) {
		Optional<User> userAux = repository.findById(userForLogout.getId());

		Map<String, Object> response = new HashMap<>();

		if (!userAux.isPresent()) {
			response.put("message", "Usuário não encontrado!");
			response.put("success", "false");

			return response;
		}

		User user = userAux.get();

		user.getSession().setSessionId("");
		repository.save(user);

		response.put("user", user);
		response.put("success", true);

		return response;
	}

	private Cookie getCookie(String name, String value) {
		Cookie c = new Cookie(name, value);
		c.setPath("/");
		return c;
	}
}
