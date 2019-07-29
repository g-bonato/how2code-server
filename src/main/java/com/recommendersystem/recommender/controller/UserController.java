package com.recommendersystem.recommender.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.recommendersystem.recommender.models.User;
import com.recommendersystem.recommender.repository.UserRepository;
import com.recommendersystem.recommender.utils.StringUtil;

@CrossOrigin(allowCredentials = "true")
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

		user.setSession(SessionController.createSession(user));
		repository.save(user);

		response.put("user", user);
		response.put("success", true);

		return response;
	}

	@RequestMapping(value = "/", method = RequestMethod.DELETE)
	public Map<String, Object> deleteUser(@RequestParam(value = "UserID", defaultValue = "") String userId,
			@RequestParam(value = "SessionID", defaultValue = "") String sessionId) {

		Map<String, Object> response = new HashMap<>();

		if (!SessionController.isValidSession(sessionId, userId)) {
			response.put("message", "Você deve estar logado!");
			response.put("success", false);

			return response;
		}

		Optional<User> userAux = repository.findById(userId);

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
	public Map<String, Object> getUserById(@RequestParam(value = "UserID", defaultValue = "") String userId,
			@RequestParam(value = "SessionID", defaultValue = "") String sessionId) {

		Map<String, Object> response = new HashMap<>();

		if (!SessionController.isValidSession(sessionId, userId)) {
			response.put("message", "Você deve estar logado!");
			response.put("success", false);

			return response;
		}

		Optional<User> userAux = repository.findById(userId);

		if (!userAux.isPresent()) {
			response.put("message", "Usuário não encontrado!");
			response.put("success", false);

			return response;
		}

		response.put("user", userAux.get());
		response.put("success", true);

		return response;
	}

	@RequestMapping(value = "/", method = RequestMethod.PUT)
	public Map<String, Object> modifyUserById(@Valid @RequestBody User user,
			@RequestParam(value = "UserID", defaultValue = "") String userId,
			@RequestParam(value = "SessionID", defaultValue = "") String sessionId) {

		Map<String, Object> response = new HashMap<>();

		if (!SessionController.isValidSession(sessionId, userId)) {
			response.put("message", "Você deve estar logado!");
			response.put("success", false);

			return response;
		}

		Optional<User> userAux = repository.findById(userId);

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
		user.setSession(SessionController.createSession(user));
		repository.save(user);

		response.put("user", user);
		response.put("success", true);

		return response;
	}

	@RequestMapping(value = "/autoLogin", method = RequestMethod.POST)
	public Map<String, Object> userAutoLogin(
			@RequestHeader(name = "Authorization", required = true, defaultValue = "") String authorization) {

		System.out.println(authorization);

		Map<String, Object> response = new HashMap<>();

		Optional<User> userAux = repository.findBySessionId(authorization);

		if (!userAux.isPresent()) {
			response.put("message", "Sessão inválida!");
			response.put("success", false);

			return response;
		}

		User user = userAux.get();

		if (!SessionController.isValidSession(authorization, user.getId())) {
			response.put("message", "Sessão inválida!");
			response.put("success", false);
		}

		response.put("user", userAux.get());
		response.put("success", true);

		return response;
	}

	@RequestMapping(value = "/logout", method = RequestMethod.POST)
	public Map<String, Object> userLogout(@RequestParam(value = "UserID", defaultValue = "") String userId,
			@RequestParam(value = "SessionID", defaultValue = "") String sessionId) {

		Map<String, Object> response = new HashMap<>();

		if (!SessionController.isValidSession(sessionId, userId)) {
			response.put("message", "Erro ao realizar o auto login");
			response.put("success", false);
		}

		Optional<User> userAux = repository.findById(userId);

		if (!userAux.isPresent()) {
			response.put("message", "Usuário não encontrado!");
			response.put("success", "false");

			return response;
		}

		User user = userAux.get();

		user.setSession(SessionController.getInvalidSession());
		repository.save(user);

		response.put("user", user);
		response.put("success", true);

		return response;
	}
}
