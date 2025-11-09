SHELL := /bin/bash

MVN ?= mvn
PORT ?= 8081
URL ?= http://localhost:$(PORT)
SPRING_LOG ?= target_spring_web_project/target/spring-boot.log
JSON ?= application/json
TTL ?= 20
NAME ?= Alice
EMAIL ?= a@x.com
ID ?= 1

.PHONY: help build run start stop restart clean enable-session session-status create-user list-users get-user update-user delete-user smoke logs watch-logs grep-flow flow-log

help:
	@echo "Targets:"
	@echo "  build           Build both modules"
	@echo "  run             Run CRUD app in foreground"
	@echo "  start           Start CRUD app in background (spring-boot:start)"
	@echo "  stop            Stop background app (spring-boot:stop)"
	@echo "  restart         Restart background app"
	@echo "  session-status  Get DebugFlow session status"
	@echo "  enable-session  Enable DebugFlow session (TTL minutes via TTL?=$(TTL))"
	@echo "  create-user     Create user (NAME?=$(NAME), EMAIL?=$(EMAIL))"
	@echo "  list-users      List users"
	@echo "  get-user        Get user by ID (ID?=$(ID))"
	@echo "  update-user     Update user (ID?=$(ID), NAME?=$(NAME), EMAIL?=$(EMAIL))"
	@echo "  delete-user     Delete user (ID?=$(ID))"
	@echo "  smoke           Enable session and exercise CRUD sequence"
	@echo "  logs            Show last 200 lines of app logs (spring-boot.log or debugflow.log)"
	@echo "  watch-logs      Tail app logs"
	@echo "  grep-flow       Show DebugFlow lines in logs"
	@echo "  flow-log        Tail pretty flow file (debugflow.log)"
	@echo "  s-chain3        Hit 3-service chain scenario"
	@echo "  s-fanout        Hit fanout/merge scenario (parallel)"
	@echo "  s-callable      Hit callable MVC async scenario"
	@echo "  s-exception     Hit exception scenario (HTTP 500)"
	@echo "  s-async         Hit @Async scenario (3 threads)"

build:
	$(MVN) -q -DskipTests install

run:
	$(MVN) -q -pl target_spring_web_project spring-boot:run

start:
	@echo "Starting CRUD app (spring-boot:start) on $(URL)"
	$(MVN) -q -pl target_spring_web_project spring-boot:start

stop:
	@echo "Stopping CRUD app (spring-boot:stop)"
	$(MVN) -q -pl target_spring_web_project spring-boot:stop || true
	@if lsof -iTCP:$(PORT) -sTCP:LISTEN -t >/dev/null 2>&1; then \
		echo "Killing process on port $(PORT)"; \
		kill $$(lsof -iTCP:$(PORT) -sTCP:LISTEN -t) || true; \
	fi

restart: stop start

clean:
	$(MVN) -q -DskipTests clean
	@rm -f debugflow.log target_spring_web_project/debugflow.log

session-status:
	curl -sS $(URL)/api/session

enable-session:
	curl -sS -X POST $(URL)/api/session/enable -H 'Content-Type: $(JSON)' -d '{"ttlMinutes":'$(TTL)'}'

create-user:
	curl -sS -X POST $(URL)/users -H 'Content-Type: $(JSON)' -d '{"name":"$(NAME)","email":"$(EMAIL)"}'

list-users:
	curl -sS $(URL)/users

get-user:
	curl -sS $(URL)/users/$(ID)

update-user:
	curl -sS -X PUT $(URL)/users/$(ID) -H 'Content-Type: $(JSON)' -d '{"name":"$(NAME)","email":"$(EMAIL)"}'

delete-user:
	curl -sS -X DELETE $(URL)/users/$(ID) -i

logs:
	@if [ -f $(SPRING_LOG) ]; then \
		echo "Showing: $(SPRING_LOG)"; \
		tail -n 200 $(SPRING_LOG); \
	elif [ -f debugflow.log ]; then \
		echo "Showing: debugflow.log"; \
		tail -n 200 debugflow.log; \
	elif [ -f target_spring_web_project/debugflow.log ]; then \
		echo "Showing: target_spring_web_project/debugflow.log"; \
		tail -n 200 target_spring_web_project/debugflow.log; \
	else \
		echo "No logs found yet. Try 'make start' and 'make smoke'"; \
	fi

watch-logs:
	@if [ -f $(SPRING_LOG) ]; then tail -f $(SPRING_LOG); \
	elif [ -f debugflow.log ]; then tail -f debugflow.log; \
	elif [ -f target_spring_web_project/debugflow.log ]; then tail -f target_spring_web_project/debugflow.log; \
	else echo "No logs to watch yet. Try 'make start' and 'make smoke'"; fi

grep-flow:
	@if [ -f $(SPRING_LOG) ]; then \
		grep -E "\\[DebugFlow\\]" $(SPRING_LOG) || true; \
	elif [ -f debugflow.log ]; then \
		cat debugflow.log | sed -n '1,200p'; \
	elif [ -f target_spring_web_project/debugflow.log ]; then \
		cat target_spring_web_project/debugflow.log | sed -n '1,200p'; \
	else \
		echo "No DebugFlow output found yet. Ensure session is enabled and endpoints called."; \
	fi

smoke: enable-session create-user list-users get-user update-user delete-user


flow-log:
	@if [ -f debugflow.log ]; then \
		echo "Tailing ./debugflow.log"; \
		tail -f debugflow.log; \
	elif [ -f target_spring_web_project/debugflow.log ]; then \
		echo "Tailing target_spring_web_project/debugflow.log"; \
		tail -f target_spring_web_project/debugflow.log; \
	else \
		echo "No debugflow.log yet. Creating placeholder and waiting for events..."; \
		touch target_spring_web_project/debugflow.log 2>/dev/null || touch debugflow.log; \
		{ [ -f target_spring_web_project/debugflow.log ] && tail -f target_spring_web_project/debugflow.log; } || tail -f debugflow.log; \
	fi

s-chain3:
	curl -sS $(URL)/scenarios/chain3

s-fanout:
	curl -sS $(URL)/scenarios/fanout

s-callable:
	curl -sS $(URL)/scenarios/callable

s-exception:
	curl -sS -i $(URL)/scenarios/exception || true

s-async:
	curl -sS $(URL)/scenarios/async
