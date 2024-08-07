package com.cleancode.real_estate_backend.services;

import com.cleancode.real_estate_backend.dtos.administrator.ticket.response.TicketResponseDTOView;
import com.cleancode.real_estate_backend.dtos.tenant.ticket.request.TicketMessageRequestDTO;
import com.cleancode.real_estate_backend.dtos.tenant.ticket.request.TicketRequestDTO;
import com.cleancode.real_estate_backend.dtos.tenant.ticket.request.TicketUpdateRequestDTO;
import com.cleancode.real_estate_backend.dtos.tenant.ticket.response.TicketMessageResponseDTO;
import com.cleancode.real_estate_backend.dtos.tenant.ticket.response.TicketResponseDTO;
import com.cleancode.real_estate_backend.dtos.tenant.ticket.response.TicketResponseDTOLite;
import com.cleancode.real_estate_backend.dtos.user.AppUserResponseDTOLite;
import com.cleancode.real_estate_backend.entities.AppUser;
import com.cleancode.real_estate_backend.entities.RentedFloor;
import com.cleancode.real_estate_backend.entities.Ticket;
import com.cleancode.real_estate_backend.entities.TicketMessage;
import com.cleancode.real_estate_backend.enums.Role;
import com.cleancode.real_estate_backend.enums.ticket.TicketDepartment;
import com.cleancode.real_estate_backend.enums.ticket.TicketSeverity;
import com.cleancode.real_estate_backend.enums.ticket.TicketStatus;
import com.cleancode.real_estate_backend.repositories.AppUserRepository;
import com.cleancode.real_estate_backend.repositories.RentedFloorRepository;
import com.cleancode.real_estate_backend.repositories.TicketMessageRepository;
import com.cleancode.real_estate_backend.repositories.TicketRepository;
import com.cleancode.real_estate_backend.utils.IAuthenticationFacade;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Log4j2
public class TicketService {
    private final TicketRepository ticketRepository;
    private final AppUserRepository appUserRepository;
    private final TicketMessageRepository ticketMessageRepository;
    private final RentedFloorRepository rentedFloorRepository;
    private final PhotoService photoService;
    private final IAuthenticationFacade authenticationFacade;

    public List<TicketResponseDTOView> getTicketsViewManager(Pageable pageable) {
        String userEmail = authenticationFacade.getAuthentication().getName();
        log.info("Fetching tickets for manager: {}", userEmail);

        AppUser manager = appUserRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    log.error("Manager not found: {}", userEmail);
                    return new EntityNotFoundException("Manager not found: " + userEmail);
                });

        return ticketRepository.findAllWithCreatorByManagerId(pageable, manager.getId())
                .stream()
                .map(this::convertToDTOView)
                .toList();
    }

    public List<TicketResponseDTOView> getTicketsViewTenant(Pageable pageable) {
        String userEmail = authenticationFacade.getAuthentication().getName();
        log.info("Fetching tickets for tenant: {}", userEmail);

        AppUser representant = appUserRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    log.error("Representant not found: {}", userEmail);
                    return new EntityNotFoundException("Representant not found: " + userEmail);
                });

        return ticketRepository.findAllWithCreatorByRepresentantId(pageable, representant.getId())
                .stream()
                .map(this::convertToDTOView)
                .toList();
    }

    private TicketResponseDTOView convertToDTOView(Ticket ticket) {
        log.info("Converting Ticket entity to TicketResponseDTOView: Ticket ID = {}", ticket.getId());

        TicketResponseDTOView dto = new TicketResponseDTOView(
                ticket.getId(),
                ticket.getSubject(),
                String.valueOf(ticket.getSeverity()),
                String.valueOf(ticket.getStatus()),
                ticket.getCreator().getId(),
                ticket.getCreator().getUsername(),
                String.valueOf(ticket.getDepartment())
        );

        log.info("Converted TicketResponseDTOView: {}", dto);
        return dto;
    }

    @Transactional
    public TicketResponseDTOLite addTicket(TicketRequestDTO ticketRequestDTO) {
        String userEmail = authenticationFacade.getAuthentication().getName();
        log.info("Adding ticket for user: {}", userEmail);

        AppUser creator = appUserRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    log.error("Creator not found: {}", userEmail);
                    return new EntityNotFoundException("Creator not found: " + userEmail);
                });

        TicketMessage ticketMessage = TicketMessage.builder()
                .message(ticketRequestDTO.message())
                .creator(creator)
                .build();

        RentedFloor rentedFloor = rentedFloorRepository.findById(ticketRequestDTO.rentedFloorId())
                .orElseThrow(() -> {
                    log.error("Rented floor not found: {}", ticketRequestDTO.rentedFloorId());
                    return new EntityNotFoundException("Rented floor not found: " + ticketRequestDTO.rentedFloorId());
                });

        Ticket ticket = new Ticket();
        ticket.setSubject(ticketRequestDTO.subject());
        ticket.setSeverity(TicketSeverity.valueOf(ticketRequestDTO.severity()));
        ticket.setRentedFloor(rentedFloor);
        ticket.setStatus(TicketStatus.PENDING);
        ticket.setDepartment(TicketDepartment.valueOf(ticketRequestDTO.department()));
        ticket.setCreator(creator);
        ticket.setResponsibleManager(rentedFloor.getFloor().getBuilding().getManager());

        Ticket savedTicket = ticketRepository.save(ticket);
        log.info("Saved new ticket: {}", savedTicket);

        ticketMessage.setTicket(savedTicket);
        TicketMessage savedTicketMessage = ticketMessageRepository.save(ticketMessage);
        log.info("Saved new ticket message: {}", savedTicketMessage);

        return new TicketResponseDTOLite(savedTicket.getId(), savedTicketMessage.getId(), ticket.getSubject());
    }

    public TicketResponseDTO getTicket(Long ticketId) {
        log.info("Fetching ticket details for ticket ID: {}", ticketId);

        Ticket ticket = ticketRepository.findWithCreatorById(ticketId)
                .orElseThrow(() -> {
                    log.error("Ticket not found: {}", ticketId);
                    return new EntityNotFoundException("Ticket not found: " + ticketId);
                });

        AppUser loggedUser = appUserRepository.findByEmail(authenticationFacade.getAuthentication().getName())
                .orElseThrow(() -> {
                    log.error("Logged user not found: {}", authenticationFacade.getAuthentication().getName());
                    return new EntityNotFoundException("Logged user not found: " + authenticationFacade.getAuthentication().getName());
                });

        Role userRole = loggedUser.getRole().stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("User has no assigned roles"));

        switch (userRole) {
            case ROLE_MANAGER:
                if (!ticket.getResponsibleManager().equals(loggedUser)) {
                    log.error("User {} is not the manager responsible for ticket ID: {}", loggedUser.getEmail(), ticketId);
                    throw new IllegalArgumentException("Only the manager of this ticket can view the details");
                }
                break;
            case ROLE_REPRESENTANT:
                boolean isRepresentant = appUserRepository.findTicketTenantRepresentatns().stream()
                        .anyMatch(appUser -> appUser.equals(loggedUser));
                if (!isRepresentant) {
                    log.error("User {} is not the representant responsible for ticket ID: {}", loggedUser.getEmail(), ticketId);
                    throw new IllegalArgumentException("Only the responsible person of this ticket can view the details");
                }
                break;
            default:
                log.error("User {} doesn't have sufficient permission", loggedUser.getEmail());
                throw new IllegalArgumentException("User doesn't have sufficient permission");
        }

        List<TicketMessage> ticketMessages = ticketMessageRepository.findAllWithImageUrlsByTicket_Id(ticket.getId());
        List<TicketMessageResponseDTO> ticketMessageResponseDTOS = new ArrayList<>();

        ticketMessages.forEach(ticketMessage -> {
            List<byte[]> photos;

            try {
                photos = photoService.getPhotos(ticketMessage.getImageUrls());
            } catch (IOException e) {
                log.error("Error reading photos for ticket message ID: {}", ticketMessage.getId(), e);
                throw new RuntimeException(e);
            }

            AppUserResponseDTOLite messageCreator = new AppUserResponseDTOLite(
                    ticketMessage.getCreator().getId(),
                    ticketMessage.getCreator().getEmail(),
                    ticketMessage.getCreator().getName()
            );

            TicketMessageResponseDTO ticketMessageResponseDTO = new TicketMessageResponseDTO(
                    ticketMessage.getId(),
                    ticketMessage.getMessage(),
                    ticketMessage.getCreateTs(),
                    photos,
                    messageCreator
            );

            ticketMessageResponseDTOS.add(ticketMessageResponseDTO);
        });

        AppUserResponseDTOLite ticketCreator = new AppUserResponseDTOLite(
                ticket.getCreator().getId(),
                ticket.getCreator().getEmail(),
                ticket.getCreator().getName()
        );

        TicketResponseDTO ticketResponseDTO = new TicketResponseDTO(
                ticket.getId(),
                String.valueOf(ticket.getSeverity()),
                String.valueOf(ticket.getStatus()),
                String.valueOf(ticket.getDepartment()),
                ticket.getSubject(),
                ticketCreator,
                ticketMessageResponseDTOS
        );

        log.info("Fetched ticket details: {}", ticketResponseDTO);
        return ticketResponseDTO;
    }

    public void addPhotosUrlsToMessage(Long ticketMessageId, Set<String> imageUrls) {
        log.info("Adding photo URLs to ticket message ID: {}", ticketMessageId);

        TicketMessage ticketMessage = ticketMessageRepository.findById(ticketMessageId)
                .orElseThrow(() -> {
                    log.error("Ticket message not found: {}", ticketMessageId);
                    return new EntityNotFoundException("Ticket message not found: " + ticketMessageId);
                });

        ticketMessage.setImageUrls(imageUrls);
        ticketMessageRepository.save(ticketMessage);
        log.info("Added photo URLs to ticket message ID: {}", ticketMessageId);
    }

    public Long countTickets() {
        String userEmail = authenticationFacade.getAuthentication().getName();
        log.info("Counting tickets for user: {}", userEmail);

        AppUser loggedUser = appUserRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    log.error("User not found: {}", userEmail);
                    return new EntityNotFoundException("User not found: " + userEmail);
                });

        Long count;
        if (loggedUser.getRole().stream().anyMatch(role -> role == Role.ROLE_MANAGER)) {
            count = ticketRepository.countTicketByResponsibleManagerId(loggedUser.getId());
        } else if (loggedUser.getRole().stream().anyMatch(role -> role == Role.ROLE_REPRESENTANT)) {
            count = ticketRepository.countTicketsByRepresentantId(loggedUser.getId());
        } else {
            log.error("User role not found for user: {}", userEmail);
            throw new IllegalArgumentException("User role not found");
        }

        log.info("Counted tickets for user {}: {}", userEmail, count);
        return count;
    }

    public TicketResponseDTOLite addMessageToTicket(Long ticketId, TicketMessageRequestDTO requestDTO) {
        String userEmail = authenticationFacade.getAuthentication().getName();
        log.info("Adding message to ticket ID: {} by user: {}", ticketId, userEmail);

        AppUser creator = appUserRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    log.error("Creator not found: {}", userEmail);
                    return new EntityNotFoundException("Creator not found: " + userEmail);
                });

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> {
                    log.error("Ticket not found: {}", ticketId);
                    return new EntityNotFoundException("Ticket not found: " + ticketId);
                });

        TicketMessage ticketMessage = TicketMessage.builder()
                .creator(creator)
                .message(requestDTO.message())
                .ticket(ticket)
                .build();

        TicketMessage savedMessage = ticketMessageRepository.save(ticketMessage);
        log.info("Added message to ticket: {}", savedMessage);

        return new TicketResponseDTOLite(ticket.getId(), savedMessage.getId(), ticket.getSubject());
    }

    public void updateTicket(Long ticketId, TicketUpdateRequestDTO ticketRequestDTO) {
        String userEmail = authenticationFacade.getAuthentication().getName();
        log.info("Updating ticket ID: {} by user: {}", ticketId, userEmail);

        AppUser loggedUser = appUserRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    log.error("User not found: {}", userEmail);
                    return new EntityNotFoundException("User not found: " + userEmail);
                });

        Ticket foundTicket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> {
                    log.error("Ticket not found: {}", ticketId);
                    return new EntityNotFoundException("Ticket not found: " + ticketId);
                });

        if (!foundTicket.getResponsibleManager().equals(loggedUser)) {
            log.error("User {} is not the manager responsible for ticket ID: {}", userEmail, ticketId);
            throw new IllegalArgumentException("Only the responsible manager of the ticket can update the ticket");
        }

        if (ticketRequestDTO.department() != null) {
            foundTicket.setDepartment(TicketDepartment.valueOf(ticketRequestDTO.department()));
        }
        if (ticketRequestDTO.severity() != null) {
            foundTicket.setSeverity(TicketSeverity.valueOf(ticketRequestDTO.severity()));
        }
        if (ticketRequestDTO.status() != null) {
            foundTicket.setStatus(TicketStatus.valueOf(ticketRequestDTO.status()));
        }

        ticketRepository.save(foundTicket);
        log.info("Updated ticket ID: {}", ticketId);
    }
}
