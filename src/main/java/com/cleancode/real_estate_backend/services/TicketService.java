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
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TicketService {
    private final TicketRepository ticketRepository;
    private final AppUserRepository appUserRepository;
    private final TicketMessageRepository ticketMessageRepository;
    private final RentedFloorRepository rentedFloorRepository;
    private final PhotoService photoService;
    private final IAuthenticationFacade authenticationFacade;

    public List<TicketResponseDTOView> getTicketsViewManager(Pageable pageable) {

        AppUser manager = appUserRepository.findByEmail(authenticationFacade.getAuthentication().getName()).orElseThrow(EntityNotFoundException::new);


        return ticketRepository.findAllWithCreatorByManagerId(pageable, manager.getId()).stream().map(this::convertToDTOView).toList();
    }

    public List<TicketResponseDTOView> getTicketsViewTenant(Pageable pageable) {

        AppUser representant = appUserRepository.findByEmail(authenticationFacade.getAuthentication().getName()).orElseThrow(EntityNotFoundException::new);

        return ticketRepository.findAllWithCreatorByRepresentantId(pageable, representant.getId()).stream().map(this::convertToDTOView).toList();
    }

    private TicketResponseDTOView convertToDTOView(Ticket ticket) {

        return new TicketResponseDTOView(
                ticket.getId(),
                ticket.getSubject(),
                String.valueOf(ticket.getSeverity()),
                String.valueOf(ticket.getStatus()),
                ticket.getCreator().getId(),
                ticket.getCreator().getUsername(),
                String.valueOf(ticket.getDepartment())
        );
    }

//    private Ticket convertToEntity(TicketRequestDTO dto) {
//
//        // Assume current user is the creator
////        AppUser creator = appUserRepository.findCurrentLoggedInUser();
//
//        Ticket entity = Ticket.builder()
//                .subject(dto.subject())
//                .severity(TicketSeverity.valueOf(dto.severity()))
//                .status(TicketStatus.PENDING) // Default status
////                .creator(creator)
//                .imageUrls(dto.imageUrls())
//                .build();
//
//        return entity;
//    }

    @Transactional
    public TicketResponseDTOLite addTicket(TicketRequestDTO ticketRequestDTO) {

        AppUser creator = appUserRepository.findByEmail(authenticationFacade.getAuthentication().getName()).orElseThrow(EntityNotFoundException::new);

        TicketMessage ticketMessage = TicketMessage.builder()
                .message(ticketRequestDTO.message())
                .creator(creator)
                .build();

        RentedFloor rentedFloor = rentedFloorRepository.findById(ticketRequestDTO.rentedFloorId()).orElseThrow(EntityNotFoundException::new);


        Ticket ticket = new Ticket();
        ticket.setSubject(ticketRequestDTO.subject());
        ticket.setSeverity(TicketSeverity.valueOf(ticketRequestDTO.severity()));
        ticket.setRentedFloor(rentedFloor);
        ticket.setStatus(TicketStatus.PENDING);
        ticket.setDepartment(TicketDepartment.valueOf(ticketRequestDTO.department()));

        ticket.setCreator(creator);

        AppUser manager = rentedFloor.getFloor().getBuilding().getManager();
        ticket.setResponsibleManager(manager);

        Ticket savedTicket = ticketRepository.save(ticket);

        ticketMessage.setTicket(savedTicket);

        TicketMessage savedTicketMessage = ticketMessageRepository.save(ticketMessage);

        return new TicketResponseDTOLite(savedTicket.getId(), savedTicketMessage.getId(), ticket.getSubject());
    }

    public TicketResponseDTO getTicket(Long ticketId) {

        Ticket ticket = ticketRepository.findWithCreatorById(ticketId).orElseThrow(EntityNotFoundException::new);

        AppUser manager = appUserRepository.findByEmail(authenticationFacade.getAuthentication().getName()).orElseThrow(EntityNotFoundException::new);

        if (!ticket.getResponsibleManager().equals(manager)) {
            throw new IllegalArgumentException("Only the manager of this ticket can view the details");
        }

        List<TicketMessage> ticketMessages = ticketMessageRepository.findAllWithImageUrlsByTicket_Id(ticket.getId());

        List<TicketMessageResponseDTO> ticketMessageResponseDTOS = new ArrayList<>();

        ticketMessages.forEach(ticketMessage -> {

            List<byte[]> photos;

            try {
                photos = photoService.getPhotos(ticketMessage.getImageUrls());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            AppUserResponseDTOLite messageCreator = new AppUserResponseDTOLite(
                    ticketMessage.getCreator().getId(),
                    ticketMessage.getCreator().getEmail(),
                    ticketMessage.getCreator().getName());

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
                ticket.getCreator().getName());


        return new TicketResponseDTO(
                ticket.getId(),
                String.valueOf(ticket.getSeverity()),
                String.valueOf(ticket.getStatus()),
                String.valueOf(ticket.getDepartment()),
                ticket.getSubject(),
                ticketCreator,
                ticketMessageResponseDTOS
        );
    }

    public void addPhotosUrlsToMessage(Long ticketMessageId, Set<String> imageUrls) {
        TicketMessage ticketMessage = ticketMessageRepository.findById(ticketMessageId).orElseThrow(EntityNotFoundException::new);
        ticketMessage.setImageUrls(imageUrls);
        ticketMessageRepository.save(ticketMessage);
    }

    public Long countTickets() {

        AppUser loggedUser = appUserRepository.findByEmail(authenticationFacade.getAuthentication().getName()).orElseThrow(EntityNotFoundException::new);

        if (loggedUser.getRole().stream()
                .anyMatch(role -> role == Role.ROLE_MANAGER)) {

            return ticketRepository.countTicketByResponsibleManagerId(loggedUser.getId());
        } else if (loggedUser.getRole().stream()
                .anyMatch(role -> role == Role.ROLE_REPRESENTANT)) {

            return ticketRepository.countTicketsByRepresentantId(loggedUser.getId());
        }

        throw new IllegalArgumentException("User not found");


    }

    public TicketResponseDTOLite addMessageToTicket(Long ticketId, TicketMessageRequestDTO requestDTO) {

        AppUser creator = appUserRepository.findByEmail(authenticationFacade.getAuthentication().getName()).orElseThrow(EntityNotFoundException::new);

        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(EntityNotFoundException::new);

        TicketMessage ticketMessage = TicketMessage.builder()
                .creator(creator)
                .message(requestDTO.message())
                .ticket(ticket)
                .build();

        TicketMessage savedMessage = ticketMessageRepository.save(ticketMessage);

        return new TicketResponseDTOLite(ticket.getId(), savedMessage.getId(), ticket.getSubject());
    }

    public void updateTicket(Long ticketId, TicketUpdateRequestDTO ticketRequestDTO) {

        AppUser loggedUser = appUserRepository.findByEmail(authenticationFacade.getAuthentication().getName()).orElseThrow(EntityNotFoundException::new);

        Ticket foundTicket = ticketRepository.findById(ticketId).orElseThrow(EntityNotFoundException::new);

        if (!foundTicket.getResponsibleManager().equals(loggedUser)) {
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
    }
}
