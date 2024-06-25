package com.cleancode.real_estate_backend.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.cleancode.real_estate_backend.repositories.AppUserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AppUserService {

    private final AppUserRepository appUserRepository;

    @Autowired
    ObjectMapper objectMapper;

//    public CampaignDTO saveCampaign(String campaignData, List<MultipartFile> files) throws IOException {
//
//        Campaign campaign = new Campaign();
//
//        JsonNode campaignDataNode = objectMapper.readTree(campaignData);
//
//        campaign.setChanel(campaignDataNode.get("chanel").asText());
//        campaign.setSelectedPackage(campaignDataNode.get("selectedPackage").asText());
//        campaign.setCaption(campaignDataNode.get("caption").asText());
//        campaign.setHashtags(campaignDataNode.get("hashtags").asText());
//        campaign.setInstagram(campaignDataNode.get("instagram").asText());
//        campaign.setEmail(campaignDataNode.get("email").asText());
//        campaign.setAdditionalInfo(campaignDataNode.get("additionalInfo").asText());
//        campaign.setPostSchedule(new Date(campaignDataNode.get("postSchedule").asLong()));
//
//        // Create and set CampaignFile instances for each file
//        Set<CampaignFile> filesSet = new HashSet<>();
//
//        for (MultipartFile file : files) {
//
//            CampaignFile campaignFile = new CampaignFile();
//
//            campaignFile.setContent(file.getBytes());
//            campaignFile.setName(file.getName());
//
//            campaignFile.setCampaign(campaign); // Set the campaign relationship
//
//            filesSet.add(campaignFile);
//        }
//
//        // Set the files in the campaign
//        campaign.setFiles(filesSet);
//
//        // Save the campaign along with its files
//        Campaign savedCampaign = campaignRepository.save(campaign);
//
//        // You can return the savedCampaign or convert it to a DTO and return if needed
//
//        return null;
//    }
//
//
//    public void saveCampaignFiles(List<MultipartFile> files, Long campaignId) throws IOException {
//
//        Campaign campaign = campaignRepository.findById(campaignId)
//                .orElseThrow(EntityNotFoundException::new);
//
//        for (MultipartFile file : files) {
//            CampaignFile campaignFile = new CampaignFile();
//            campaignFile.setContent(file.getBytes());
//            campaignFile.setCampaign(campaign);
//
//            campaign.getFiles().add(campaignFile);
//        }
//
//        campaignRepository.save(campaign);
//    }
}
