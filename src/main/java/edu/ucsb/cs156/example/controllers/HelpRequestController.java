package edu.ucsb.cs156.example.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.ucsb.cs156.example.entities.HelpRequest;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.HelpRequestRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** This is a REST controller for HelpRequests */
@Tag(name = "HelpRequest")
@RequestMapping("/api/helprequest")
@RestController
@Slf4j
public class HelpRequestController extends ApiController {

  @Autowired HelpRequestRepository helpRequestRepository;

  /**
   * List all help requests
   *
   * @return an iterable of HelpRequest
   */
  @Operation(summary = "List all help requests")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<HelpRequest> allHelpRequests() {
    Iterable<HelpRequest> helpRequests = helpRequestRepository.findAll();
    return helpRequests;
  }

  /*
   * Create a new help request
   * String requesterEmail
   * String teamId
   * String tableOrBreakoutRoom
   * LocalDateTime requestTime
   * String explanation
   * boolean solved
   *
   * @return the posted HelpRequest
   */
  @Operation(summary = "Create a new help request")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public HelpRequest postHelpRequest(
      @Parameter(name = "requesterEmail") @RequestParam String requesterEmail,
      @Parameter(name = "teamId") @RequestParam String teamId,
      @Parameter(name = "tableOrBreakoutRoom") @RequestParam String tableOrBreakoutRoom,
      @Parameter(
              name = "requestTime",
              description =
                  "in iso format - YYYY-MM-DDTHH:MM:SS, see in https://en.wikipedia.org/wiki/ISO_8601, e.g. 2007-03-01T13:00:00")
          @RequestParam("requestTime")
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime reqDateTime,
      @Parameter(name = "explanation") @RequestParam String explanation,
      @Parameter(name = "solved") @RequestParam boolean solved)
      throws JsonProcessingException {
    HelpRequest helpRequest = new HelpRequest();
    helpRequest.setRequesterEmail(requesterEmail);
    helpRequest.setTeamId(teamId);
    helpRequest.setTableOrBreakoutRoom(tableOrBreakoutRoom);
    helpRequest.setRequestTime(reqDateTime);
    helpRequest.setExplanation(explanation);
    helpRequest.setSolved(solved);
    HelpRequest savedHelpRequest = helpRequestRepository.save(helpRequest);
    return savedHelpRequest;
  }

  /**
   * Get a HelpRequest by id
   *
   * @param id the id of the HelpRequest
   * @return the HelpRequest if it is found
   */
  @Operation(summary = "Get a help request")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("")
  public HelpRequest getById(@Parameter(name = "id") @RequestParam Long id) {

    return helpRequestRepository
        .findById(id)
        .orElseThrow(() -> new EntityNotFoundException(HelpRequest.class, id));
  }

  /**
   * Update a HelpRequest by id
   *
   * @param id the id of the HelpRequest
   * @param update the HelpRequest containing the updates
   * @return the updated HelpRequest
   */
  @Operation(summary = "Update a help request")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PutMapping("")
  public HelpRequest updateHelpRequest(
      @Parameter(name = "id") @RequestParam Long id, @RequestBody @Valid HelpRequest update) {

    HelpRequest existing =
        helpRequestRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(HelpRequest.class, id));

    existing.setRequesterEmail(update.getRequesterEmail());
    existing.setTeamId(update.getTeamId());
    existing.setTableOrBreakoutRoom(update.getTableOrBreakoutRoom());
    existing.setRequestTime(update.getRequestTime());
    existing.setExplanation(update.getExplanation());
    existing.setSolved(update.getSolved());

    helpRequestRepository.save(existing);
    return existing;
  }

  @Operation(summary = "Delete a help request")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @DeleteMapping("")
  public Object deleteHelpRequest(@Parameter(name = "id") @RequestParam Long id) {
    HelpRequest existing =
        helpRequestRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(HelpRequest.class, id));

    helpRequestRepository.delete(existing);
    return genericMessage("HelpRequest with id %s deleted".formatted(id));
  }
}
